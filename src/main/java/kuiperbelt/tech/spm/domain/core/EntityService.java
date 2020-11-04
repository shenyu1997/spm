package kuiperbelt.tech.spm.domain.core;

import kuiperbelt.tech.spm.common.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
