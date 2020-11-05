package kuiperbelt.tech.spm.domain.idmapping;

import kuiperbelt.tech.spm.common.BaseEntity;
import kuiperbelt.tech.spm.domain.event.Event;
import kuiperbelt.tech.spm.domain.event.EventRepository;
import kuiperbelt.tech.spm.domain.core.Project;
import kuiperbelt.tech.spm.domain.core.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@RepositoryEventHandler
public class EntityService {

    @Autowired
    private IdTypeRepository idTypeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

    @HandleAfterCreate
    public void postHandleEntityCreate(BaseEntity entity) {
        if(entity instanceof IdType) {
            return;
        }
        idTypeRepository.save(IdType.builder()
                .target(entity.getId())
                .type(entity.getClass().getSimpleName())
                .build());
    }

    public <T extends BaseEntity> T getEntity(Long targetEntityId) {
        IdType byTarget = idTypeRepository.findByTarget(targetEntityId);
        Optional<? extends BaseEntity> result;
        switch (byTarget.getType()) {
            case Project.ENTITY_TYPE:
                result =  projectRepository.findById(targetEntityId);
                break;
            case Event.ENTITY_TYPE:
                result = eventRepository.findById(targetEntityId);
                break;
            default:
                result = Optional.empty();
        }
        return (T) result.orElseThrow(()-> new IllegalArgumentException("targetEntityId is not found"));
    }
}
