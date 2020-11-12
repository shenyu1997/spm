package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.common.AuditDelegate;
import tech.kuiperbelt.spm.common.AuditListener;
import tech.kuiperbelt.spm.common.AuditableEntity;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@FieldNameConstants
@Audited
@EntityListeners(AuditListener.class)
@Getter
@Setter
@Entity
@ToString
@Table(name = "projects")
public class Project extends BaseEntity implements AuditableEntity, ExecutableEntity {

    @NotNull
    private String name;

    private String description;

    private String owner;

    private String manager;

    @ElementCollection
    private List<String> members = new ArrayList<>();

    @OneToMany(mappedBy = "project")
    private List<Phase> phases;

    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    @JsonIgnore
    @Embedded
    @Delegate
    private ExecutableDelegate executableDelegate = new ExecutableDelegate();

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

    public List<Phase> getPhases() {
        if(this.phases == null) {
            return Collections.emptyList();
        }

        return phases.stream()
                .sorted(Comparator.comparing(Phase::getSeq))
                .collect(Collectors.toList());
    }
}
