package tech.kuiperbelt.spm.domain.core.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Component
public class AuditListener {

    @Autowired
    private UserContextHolder userContextHolder;

    @PrePersist
    @PreUpdate
    @PreRemove
    private void beforeAnyOperation(Object object) {
        if(object instanceof AuditableEntity) {
            String upn = userContextHolder.getUserContext().getUpn();
            AuditableEntity baseEntity = (AuditableEntity) object;
            baseEntity.setOperator(upn);
            baseEntity.setTimestamp(LocalDateTime.now());
        }
    }
}
