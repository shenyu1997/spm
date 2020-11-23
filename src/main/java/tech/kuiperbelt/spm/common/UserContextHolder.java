package tech.kuiperbelt.spm.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.util.UUID;
import java.util.function.Supplier;

@Component
public class UserContextHolder extends TransactionSynchronizationAdapter {

    @Autowired
    private UserContext userContext;

    private TransactionTemplate transactionTemplate;

    private ThreadLocal<UserContext> userContextThreadLocal = new ThreadLocal<>();

    @Autowired
    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    public UserContext getUserContext() {
        if(userContextThreadLocal.get() != null) {
            return userContextThreadLocal.get();
        } else {
            if(!userContext.isInit()) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Assert.state(authentication != null, "authentication can not be null");
                userContext.setUpn(authentication.getName());
                userContext.setCorrelationId(UUID.randomUUID().toString());
                userContext.setInit(true);
            }
            return userContext;
        }
    }

    public void runAs(UserContext userContext, Runnable runnable) {
        runAs(userContext, () -> {
            runnable.run();
            return null;
        });
    }

    public <R> R runAs(UserContext userContext, Supplier<R> supplier) {
        Assert.isNull(userContextThreadLocal.get(), "RunAs can not call by re-entering");
        return transactionTemplate.execute(status -> {
            try {
                userContextThreadLocal.set(userContext);
                return supplier.get();
            } finally {
                TransactionSynchronizationManager.registerSynchronization(this);
            }
        });
    }

    @Override
    public void afterCompletion(int status) {
        userContextThreadLocal.remove();
    }
}
