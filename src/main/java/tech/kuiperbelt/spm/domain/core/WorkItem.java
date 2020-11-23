package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import tech.kuiperbelt.spm.common.AuditDelegate;
import tech.kuiperbelt.spm.common.AuditListener;
import tech.kuiperbelt.spm.common.AuditableEntity;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

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
public class WorkItem extends BaseEntity implements AuditableEntity, ExecutableEntity {

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

    @NotAudited
    @OneToMany(mappedBy = Note.Fields.workItem)
    private List<Note> notes;

    @Builder.Default
    @JsonIgnore
    @Embedded
    @lombok.experimental.Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate
    private ExecutableDelegate executableDelegate = new ExecutableDelegate();


    public enum Priority {
        LOW, MEDIUM, HIGH, TOP
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
        }
        return null;
    }

    public boolean move(Period offset) {
        boolean moved = false;
        if(this.plannedStartDate!= null) {
            this.plannedStartDate = this.plannedStartDate.plus(offset);
            moved = true;
        }
        if(this.deadLine != null) {
            this.deadLine = this.deadLine.plus(offset);
            moved = true;
        }
        return  moved;
    }
}
