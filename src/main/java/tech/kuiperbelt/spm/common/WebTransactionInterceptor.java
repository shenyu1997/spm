package tech.kuiperbelt.spm.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

@Slf4j
@Component
public class WebTransactionInterceptor implements WebRequestInterceptor {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private ThreadLocal<TransactionStatus> transactionStatusThreadLocal = new ThreadLocal<>();


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
        TransactionStatus status = transactionStatusThreadLocal.get();
        try {
            if(status.isCompleted()) {
                return;
            }
            if (ex != null || status.isRollbackOnly()) {
                platformTransactionManager.rollback(status);
            } else {
                platformTransactionManager.commit(status);
            }
            if(log.isDebugEnabled()) {
                log.debug("SPM-DATA-REST-TX end");
            }
        } finally {
            transactionStatusThreadLocal.remove();
        }
    }

    @Override
    public void postHandle(WebRequest webRequest, ModelMap modelMap) throws Exception {

    }

}
