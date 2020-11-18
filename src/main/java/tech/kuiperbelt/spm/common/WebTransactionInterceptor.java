package tech.kuiperbelt.spm.common;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;
import tech.kuiperbelt.spm.domain.event.Event;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class WebTransactionInterceptor implements WebRequestInterceptor {

    public static final String SPM_DATA_REST_TX = "SPM-DATA-REST-TX";
    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ExceptionHandlerAdvice exceptionHandlerAdvice;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void preHandle(WebRequest webRequest) throws Exception {
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            if(log.isDebugEnabled()) {
                log.debug(SPM_DATA_REST_TX + " continue");
            }
        } else {
            DefaultTransactionDefinition definition = getDefaultTransactionDefinition();
            TransactionStatus status = platformTransactionManager.getTransaction(definition);
            TransactionSynchronizationManager.bindResource(this, status);
            if(log.isDebugEnabled()) {
                log.debug(SPM_DATA_REST_TX + " start");
            }
        }
        applicationEventPublisher.publishEvent(Event.BULK_BEGIN);
    }

    private DefaultTransactionDefinition getDefaultTransactionDefinition() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName(SPM_DATA_REST_TX);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        definition.setTimeout(3000);
        return definition;
    }

    @Override
    public void afterCompletion(WebRequest webRequest, Exception ex) throws Exception {
        applicationEventPublisher.publishEvent(Event.BULK_END);
        if(!TransactionSynchronizationManager.hasResource(this)) {
            return;
        }
        try {
            TransactionStatus status = (TransactionStatus) TransactionSynchronizationManager.getResource(this);
            if (status.isCompleted()) {
                return;
            }
            boolean needRollback = ex != null || status.isRollbackOnly();
            if (needRollback) {
                platformTransactionManager.rollback(status);
            } else {
                platformTransactionManager.commit(status);
            }
            if (log.isDebugEnabled()) {
                log.debug(SPM_DATA_REST_TX + " {}", needRollback? "rollback": "commit");
            }
        } catch (Exception e) {
            // We need handle exception in case it is thrown by "Commit" phase.
            // Call ExceptionHandlerAdvice get receive uniform error message.
            if(webRequest instanceof DispatcherServletWebRequest) {
                DispatcherServletWebRequest dispatcherServletWebRequest = (DispatcherServletWebRequest) webRequest;
                HttpServletResponse response = dispatcherServletWebRequest.getResponse();
                ResponseEntity<ExceptionHandlerAdvice.ErrorMessage> error = exceptionHandlerAdvice.handle(e);
                response.setStatus(error.getStatusCodeValue());
                response.getWriter().write(objectMapper.writeValueAsString(error.getBody()));
                response.flushBuffer();
            }
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(this);
        }
    }

    @Override
    public void postHandle(WebRequest webRequest, ModelMap modelMap) throws Exception {

    }

}
