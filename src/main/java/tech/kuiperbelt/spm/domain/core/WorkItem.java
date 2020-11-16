package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import tech.kuiperbelt.spm.common.AuditDelegate;
import tech.kuiperbelt.spm.common.AuditListener;
import tech.kuiperbelt.spm.common.AuditableEntity;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Period;

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
@Table(name = "work_items")
public class WorkItem extends BaseEntity implements AuditableEntity, ExecutableEntity {

    @NotNull
    private String name;

    private boolean ready;

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

    @JsonIgnore
    @Embedded
    @lombok.experimental.Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    @JsonIgnore
    @Embedded
    @Delegate
    private ExecutableDelegate executableDelegate = new ExecutableDelegate();

    private enum Priority {
        LOW, MEDIUM, HIGH, TOP
    }

    public boolean isOverflow() {
        return getPhase().isOverflowBy(getPlannedStartDate()) ||
                getPhase().isOverflowBy(getDeadLine());
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
