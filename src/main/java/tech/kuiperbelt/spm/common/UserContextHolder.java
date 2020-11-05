package tech.kuiperbelt.spm.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserContextHolder {

    @Autowired
    private UserContext userContext;

    public UserContext getUserContext() {
        if(!userContext.isInit()) {
            userContext.setUpn(SecurityContextHolder.getContext().getAuthentication().getName());
            userContext.setCorrelationId(UUID.randomUUID().toString());
            userContext.setInit(true);
        }
        return userContext;
    }
}
