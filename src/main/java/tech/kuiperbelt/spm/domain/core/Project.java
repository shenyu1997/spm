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
public class Project extends BaseEntity implements AuditableEntity {

    @NotNull
    private String name;

    private String description;

    private String owner;

    private String manager;

    @ElementCollection
    private List<String> members = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RunningStatus status;

    private boolean cancelled;

    @OneToMany(mappedBy = "project")
    private List<Phase> phases;

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

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

    public List<Phase> getPhases() {
        if(this.phases == null) {
            return Collections.emptyList();
        }

        return phases.stream()
                .sorted(Comparator.comparing(Phase::getSeq))
                .collect(Collectors.toList());
    }

    public void start() {
        Assert.isTrue(this.getStatus() == RunningStatus.INIT, "Only INIT project can be started");
        this.setStatus(RunningStatus.RUNNING);
        this.setActualStartDate(LocalDate.now());
    }

    public boolean isCanBeStarted() {
        return this.getStatus() == RunningStatus.INIT;
    }

    public void done() {
        Assert.isTrue(this.getStatus() == RunningStatus.RUNNING, "Only RUNNING project can be done");
        this.setStatus(RunningStatus.STOP);
        this.setActualEndDate(LocalDate.now());
    }

    public boolean isCanBeDone() {
        return this.getStatus() == RunningStatus.RUNNING;
    }

    public void cancel() {
        Assert.isTrue(this.getStatus() != RunningStatus.STOP, "STOP project can not be cancelled");
        this.setStatus(RunningStatus.STOP);
        this.setCancelled(true);
    }

    public boolean isCanBeCancelled() {
        return this.getStatus() != RunningStatus.STOP;
    }

    public boolean isCanBeRemoved() {
        return RunningStatus.STOP == getStatus() && isCancelled();
    }
}
