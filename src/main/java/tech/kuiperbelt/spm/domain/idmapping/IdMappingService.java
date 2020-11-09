package tech.kuiperbelt.spm.domain.idmapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.kuiperbelt.spm.common.BaseEntity;
import tech.kuiperbelt.spm.domain.core.PhaseRepository;
import tech.kuiperbelt.spm.domain.core.ProjectRepository;
import tech.kuiperbelt.spm.domain.event.EventRepository;

import java.util.Optional;

@Transactional
@Service
@RepositoryEventHandler
public class IdMappingService {
    public final static String ENTITY_TYPE_EVENT = "Event";
    public final static String ENTITY_TYPE_PROJECT = "Project";
    public final static String ENTITY_TYPE_PHASE = "Phase";

    @Autowired
    private IdMappingRepository idMappingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PhaseRepository phaseRepository;


    @HandleAfterCreate
    public void postHandleEntityCreate(BaseEntity entity) {
        idMappingRepository.save(IdMapping.builder()
                .id(entity.getId())
                .type(entity.getClass().getSimpleName())
                .build());
    }

    @HandleAfterDelete
    public void postHandleEntityDelete(BaseEntity entityToBeDelete) {
        idMappingRepository.deleteById(entityToBeDelete.getId());
    }

    public Optional<? extends BaseEntity> findEntity(Long entityId) {
        Optional<IdMapping> byTarget = idMappingRepository.findById(entityId);
        if(!byTarget.isPresent()) {
            return Optional.empty();
        }
        switch (byTarget.get().getType()) {
            case ENTITY_TYPE_PROJECT:
                return projectRepository.findById(entityId);
            case ENTITY_TYPE_EVENT:
                return eventRepository.findById(entityId);
            case ENTITY_TYPE_PHASE:
                return phaseRepository.findById(entityId);
            default:
                return Optional.empty();
        }
    }
}
