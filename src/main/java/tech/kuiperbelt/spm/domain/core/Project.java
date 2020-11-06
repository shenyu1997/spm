package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.hibernate.envers.Audited;
import tech.kuiperbelt.spm.common.AuditDelegate;
import tech.kuiperbelt.spm.common.AuditListener;
import tech.kuiperbelt.spm.common.AuditableEntity;
import tech.kuiperbelt.spm.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Audited
@EntityListeners(AuditListener.class)
@Getter
@Setter
@Entity
@ToString
@Table(name = "projects")
public class Project extends BaseEntity implements AuditableEntity {
    public final static String ENTITY_TYPE = "Project";

    private String name;

    private String description;

    private String owner;

    private String manager;

    @ElementCollection
    private List<String> members = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RunningStatus status;

    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    @Version
    private long version;

    public Set<String> getParticipants() {
        Set result = new HashSet();
        if(!CollectionUtils.isEmpty(members)) {
            result.addAll(members);
        }
        result.add(owner);
        return result;
    }
}
