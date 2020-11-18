package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
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
import java.util.*;
import java.util.stream.Collectors;

@FieldNameConstants
@Audited
@EntityListeners(AuditListener.class)
@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "projects")
public class Project extends BaseEntity implements AuditableEntity, ExecutableEntity {

    @NotNull
    private String name;

    private String description;

    private String owner;

    private String manager;

    @Builder.Default
    @ElementCollection
    private List<String> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = Phase.Fields.project)
    private List<Phase> phases = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate(excludes = ProjectExecutableExclude.class)
    private ExecutableDelegate executableDelegate = new ExecutableDelegate();

    @Version
    private long version;

    public Set<String> getParticipants() {
        Set<String> result = new HashSet<>();
        if(!CollectionUtils.isEmpty(members)) {
            result.addAll(members);
        }
        result.add(owner);
        return result;
    }

    public List<Phase> getPhases() {
        if(this.phases == null) {
            return new LinkedList<>();
        }

        return phases.stream()
                .sorted(Comparator.comparing(Phase::getSeq))
                .collect(Collectors.toList());
    }

    @Builder.Default
    @JsonIgnore
    private boolean allPhasesStop = true;


    public void checkAllPhaseStop () {
        allPhasesStop = this.getPhases().stream()
                .allMatch(phase -> phase.getStatus() == RunningStatus.STOP);
    }

    @Override
    public boolean isCanBeDone() {
        return allPhasesStop && executableDelegate.isCanBeDone();
    }

    @Override
    public void done() {
        Assert.isTrue(allPhasesStop, "all phases stop");
        executableDelegate.done();
    }

    private interface ProjectExecutableExclude {
        void isCanBeDone();
        void done();
    }
}
