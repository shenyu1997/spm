package tech.kuiperbelt.spm.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.UUID;

@Component
public class UserContextHolder {

    @Autowired
    private UserContext userContext;

    public UserContext getUserContext() {
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
