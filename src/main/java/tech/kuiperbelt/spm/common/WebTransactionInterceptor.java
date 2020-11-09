package tech.kuiperbelt.spm.common;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;
import tech.kuiperbelt.spm.domain.event.Event;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class WebTransactionInterceptor implements WebRequestInterceptor {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private ThreadLocal<TransactionStatus> transactionStatusThreadLocal = new ThreadLocal<>();

    @Autowired
    private ExceptionHandlerAdvice exceptionHandlerAdvice;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void preHandle(WebRequest webRequest) throws Exception {
        Assert.isNull(transactionStatusThreadLocal.get(), "TransactionStatusThreadLocal supposed be null");
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("SPM-DATA-REST-TX");
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        definition.setTimeout(3000);
        TransactionStatus status = platformTransactionManager.getTransaction(definition);
        transactionStatusThreadLocal.set(status);
        if(log.isDebugEnabled()) {
            log.debug("SPM-DATA-REST-TX start");
        }
    }



    @Override
    public void afterCompletion(WebRequest webRequest, Exception ex) throws Exception {
        applicationEventPublisher.publishEvent(Event.BULK_END);
        TransactionStatus status = transactionStatusThreadLocal.get();
        try {
            if (status.isCompleted()) {
                return;
            }
            if (ex != null || status.isRollbackOnly()) {
                platformTransactionManager.rollback(status);
            } else {
                platformTransactionManager.commit(status);
            }
            if (log.isDebugEnabled()) {
                log.debug("SPM-DATA-REST-TX end");
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
            transactionStatusThreadLocal.remove();
        }
    }

    @Override
    public void postHandle(WebRequest webRequest, ModelMap modelMap) throws Exception {

    }

}
