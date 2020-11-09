package tech.kuiperbelt.spm.domain.core;

import lombok.Setter;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Setter
@Service
@RepositoryEventHandler
public class PhaseService {
    @HandleBeforeCreate
    public void preHandleProjectCreate(Phase phase) {

    }

    @HandleAfterCreate
    public void postHandleProjectCreate(Phase phase) {

    }
}
