package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.support.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

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
@Table(name = "work_items")
public class WorkItem extends ExecutableEntity implements AuditableEntity {

    @ToString.Include
    @NotNull
    private String name;

    private String detail;

    private Boolean ready;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private boolean milestone;

    private LocalDate plannedStartDate;

    private LocalDate deadLine;

    @ManyToOne
    private Phase phase;

    @ManyToOne
    private Project project;

    private String owner;

    private String assignee;

    @Enumerated(EnumType.STRING)
    private Scope scope;

    @Version
    private Long version;

    @Builder.Default
    @JsonIgnore
    @Embedded
    @lombok.experimental.Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    void determineScope() {
        if(getPhase() != null) {
            Assert.isTrue(getPhase().getStatus() != RunningStatus.STOP, "Phase can not be STOP");
        }

        if(getProject()!= null) {
            Assert.isTrue(getProject().getStatus() != RunningStatus.STOP, "Project can not be STOP");
        }

        Assert.isTrue(getPhase() == null ||
                        getProject() == null ||
                Objects.equals(getPhase().getProject(), getProject()),
                "Phase and Project was not match");

        if(getPhase() != null) {
            setScope(Scope.PHASE);
            setProject(getPhase().getProject());
            // set allItem stop false
            getPhase().setAllItemStop(false);
        } else if(getProject() != null) {
            setScope(Scope.PROJECT);
            // set allDirItem stop false
            getProject().setAllDirItemsStop(false);
        } else {
            setScope(Scope.PERSON);
        }
    }


    public LocalDate getPlannedStartDate() {
        if(plannedStartDate == null && getPhase() != null) {
            return getPhase().getPlannedStartDate();
        } else {
            return plannedStartDate;
        }
    }

    public LocalDate getDeadLine() {
        if(deadLine == null && getPhase() != null) {
            return getPhase().getPlannedEndDate();
        } else {
            return deadLine;
        }
    }

    public boolean isOverflow() {
        if(getPhase() != null) {
            return getPhase().isOverflowBy(getPlannedStartDate()) ||
                    getPhase().isOverflowBy(getDeadLine());
        } else {
            return false;
        }
    }

    public Boolean getReady() {
        if(ready != null) {
            return ready;
        } else if(getPhase() != null) {
            return getPhase().getStatus() != RunningStatus.INIT;
        } else if(getId() != null) {
            return true;
        } else {
            return null;
        }
    }

    public void move(Period offset) {
        if(this.plannedStartDate!= null) {
            this.plannedStartDate = this.plannedStartDate.plus(offset);
        }
        if(this.deadLine != null) {
            this.deadLine = this.deadLine.plus(offset);
        }
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, TOP
    }

    public enum Scope {
        PERSON, PHASE, PROJECT
    }
}
