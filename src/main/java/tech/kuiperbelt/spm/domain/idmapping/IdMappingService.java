package tech.kuiperbelt.spm.domain.idmapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.kuiperbelt.spm.common.BaseEntity;
import tech.kuiperbelt.spm.domain.core.PhaseRepository;
import tech.kuiperbelt.spm.domain.core.ProjectRepository;
import tech.kuiperbelt.spm.domain.event.EventRepository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static tech.kuiperbelt.spm.domain.idmapping.IdMapping.ID_MAPPINGS;

@Transactional
@Service
public class IdMappingService {
    public final static String ENTITY_TYPE_EVENT = "Event";
    public final static String ENTITY_TYPE_PROJECT = "Project";
    public final static String ENTITY_TYPE_PHASE = "Phase";
    public static final String DELETE_SQL = "delete from " + ID_MAPPINGS + " where id=?";

    @Autowired
    private IdMappingRepository idMappingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PhaseRepository phaseRepository;

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
