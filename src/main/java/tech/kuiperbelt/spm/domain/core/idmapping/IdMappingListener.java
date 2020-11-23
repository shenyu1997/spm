package tech.kuiperbelt.spm.domain.core.idmapping;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;

@Setter
@Component
public class IdMappingListener {
    @Autowired
    private IdMappingService idMappingService;

    @PostPersist
    private void postPersist(BaseEntity entity) {
        idMappingService.postHandleEntityCreate(entity);
    }

    @PostRemove
    private void postRemove(BaseEntity entity) {
        idMappingService.postHandleEntityDelete(entity);
    }
}
