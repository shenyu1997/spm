package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import tech.kuiperbelt.spm.domain.core.support.AuditDelegate;
import tech.kuiperbelt.spm.domain.core.support.AuditListener;
import tech.kuiperbelt.spm.domain.core.support.AuditableEntity;
import tech.kuiperbelt.spm.domain.core.support.ExecutableEntity;

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
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "projects")
public class Project extends ExecutableEntity implements AuditableEntity {

    @ToString.Include
    @NotNull
    private String name;

    private String description;

    private String owner;

    private String manager;

    @Version
    private Long version;

    @Builder.Default
    @ElementCollection
    private List<String> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = Phase.Fields.project)
    private List<Phase> phases = new ArrayList<>();

    @Builder.Default
    @RestResource(path = "work-items")
    @OneToMany(mappedBy = WorkItem.Fields.project)
    private List<WorkItem> workItems = new ArrayList<>();


    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();


    public Set<String> getParticipants() {
        Set<String> result = new HashSet<>();
        if(!CollectionUtils.isEmpty(getMembers())) {
            result.addAll(getMembers());
        }
        result.add(getOwner());
        result.add(getManager());
        return result;
    }

    @JsonIgnore
    public List<WorkItem> getDirectWorkItems() {
        return getWorkItems().stream()
                .filter(workItem -> WorkItem.Scope.PROJECT == workItem.getScope())
                .collect(Collectors.toList());
    }

    public List<Phase> getPhases() {
        if(phases == null) {
            return new LinkedList<>();
        }

        return phases.stream()
                .sorted(Comparator.comparing(Phase::getSeq))
                .collect(Collectors.toList());
    }

    @Builder.Default
    @JsonIgnore
    private boolean allPhasesStop = true;

    @Builder.Default
    @JsonIgnore
    private boolean allDirItemsStop = true;


    public void checkAllPhaseStop () {
        setAllPhasesStop(this.getPhases().stream()
                .allMatch(phase -> phase.getStatus() == RunningStatus.STOP));
    }

    public void checkAllDirItemsStop () {
        setAllDirItemsStop(this.getDirectWorkItems().stream()
                .allMatch(workItem -> workItem.getStatus() == RunningStatus.STOP));
    }

    public void checkAllDirItemsStopAfterRemove (WorkItem target) {
        List<WorkItem> directWorkItems = new ArrayList<>(this.getDirectWorkItems());
        directWorkItems.remove(target);
        setAllDirItemsStop(directWorkItems.stream()
                .allMatch(workItem -> workItem.getStatus() == RunningStatus.STOP));
    }

    @Override
    public boolean isCanBeDone() {
        return isAllPhasesStop() &&
                isAllDirItemsStop() &&
                super.isCanBeDone();
    }

    @Override
    public void done() {
        Assert.isTrue(isCanBeDone(), "all phases and all item should be stop");
        super.done();
    }
}
