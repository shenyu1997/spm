package tech.kuiperbelt.spm.domain.idmapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.kuiperbelt.spm.common.BaseEntity;
import tech.kuiperbelt.spm.domain.core.ProjectRepository;
import tech.kuiperbelt.spm.domain.event.EventRepository;

import java.util.Optional;

@Transactional
@Service
@RepositoryEventHandler
public class IdMappingService {
    public final static String ENTITY_TYPE_PROJECT = "Project";
    public final static String ENTITY_TYPE_EVENT = "Event";

    @Autowired
    private IdMappingRepository idMappingRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

    @HandleAfterCreate
    public void postHandleEntityCreate(BaseEntity entity) {
        idMappingRepository.save(IdMapping.builder()
                .id(entity.getId())
                .type(entity.getClass().getSimpleName())
                .build());
    }

    public <T extends BaseEntity> T getEntity(Long entityId) {
        IdMapping byTarget = idMappingRepository.findById(entityId)
                .orElseThrow(this::idNotFound);
        Optional<? extends BaseEntity> result;
        switch (byTarget.getType()) {
            case ENTITY_TYPE_PROJECT:
                result =  projectRepository.findById(entityId);
                break;
            case ENTITY_TYPE_EVENT:
                result = eventRepository.findById(entityId);
                break;
            default:
                result = Optional.empty();
        }
        return (T) result
                .orElseThrow(this::idNotFound);
    }

    private IllegalArgumentException idNotFound() {
        return new IllegalArgumentException("Id: is not found within all entities");
    }
}
