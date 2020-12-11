package tech.kuiperbelt.spm.domain.core.idmapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import tech.kuiperbelt.spm.domain.core.*;
import tech.kuiperbelt.spm.domain.core.event.Event;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;
import tech.kuiperbelt.spm.domain.core.event.EventRepository;

import javax.sql.DataSource;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static tech.kuiperbelt.spm.domain.core.idmapping.IdMapping.ID_MAPPINGS;

@Transactional
@Service
public class IdMappingService {
    public final static String ENTITY_TYPE_EVENT = "Event";
    public final static String ENTITY_TYPE_PROJECT = "Project";
    public final static String ENTITY_TYPE_PHASE = "Phase";
    public final static String ENTITY_TYPE_WORK_ITEM = "WorkItem";
    public final static String ENTITY_TYPE_NOTE = "Note";
    public static final String DELETE_SQL = "delete from " + ID_MAPPINGS + " where id=?";

    @Autowired
    private IdMappingRepository idMappingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PhaseRepository phaseRepository;

    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Lazy
    @Autowired
    private RepositoryEntityLinks entityLinks;

    private NamedParameterJdbcOperations namedParameterJdbcOperations;

    private SimpleJdbcInsert insertActor;

    private JdbcOperations jdbcOperations;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        insertActor = new SimpleJdbcInsert(dataSource).withTableName(ID_MAPPINGS);
        jdbcOperations = new JdbcTemplate(dataSource);
    }

    public void postHandleEntityCreate(BaseEntity entity) {
        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put(IdMapping.Fields.id, entity.getId());
        parameters.put(IdMapping.Fields.type, entity.getClass().getSimpleName());
        insertActor.execute(parameters);
    }

    public void postHandleEntityDelete(BaseEntity entityToBeDelete) {
        jdbcOperations.update(DELETE_SQL,entityToBeDelete.getId());
    }

    public Optional<Class<? extends BaseEntity>> findEntityType(Long entityId) {
        Optional<IdMapping> byTarget = idMappingRepository.findById(entityId);
        if(!byTarget.isPresent()) {
            return Optional.empty();
        }
        switch (byTarget.get().getType()) {
            case ENTITY_TYPE_PROJECT:
                return Optional.of(Project.class);
            case ENTITY_TYPE_EVENT:
                return Optional.of(Event.class);
            case ENTITY_TYPE_PHASE:
                return Optional.of(Phase.class);
            case ENTITY_TYPE_WORK_ITEM:
                return Optional.of(WorkItem.class);
            case ENTITY_TYPE_NOTE:
                return Optional.of(Note.class);
            default:
                return Optional.empty();
        }
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
            case ENTITY_TYPE_WORK_ITEM:
                return workItemRepository.findById(entityId);
            case ENTITY_TYPE_NOTE:
                return noteRepository.findById(entityId);
            default:
                return Optional.empty();
        }
    }

    public Optional<Link> toEntityLink(Long entityId) {
        return findEntity(entityId)
                .map(entity ->
                        entityLinks.linkToItemResource(entity.getClass(), entityId));

    }

    public Optional<Link> toEntityIdsLink(List<Long> entityIds, String rel) {
        if(entityIds.isEmpty()) return Optional.empty();
        return entityIds.stream()
                .findFirst()
                .flatMap(this::findEntityType)
                .map(entityType -> entityLinks.linksToSearchResources(entityType))
                .flatMap(links -> links.getLink("findByIds"))
                .map(searchLink -> getUri(searchLink, entityIds))
                .map(uri -> Link.of(uri.toString(), rel));
    }

    private URI getUri(Link searchLink, List<Long> ids) {
        String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return UriComponentsBuilder
                .fromUri(searchLink.toUri())
                .queryParam("ids", idsStr)
                .build()
                .toUri();
    }
}
