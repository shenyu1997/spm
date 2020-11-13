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
@Table(name = "phases", uniqueConstraints={@UniqueConstraint(columnNames={"id","seq"})})
public class Phase extends BaseEntity implements AuditableEntity, ExecutableEntity {
    @NotNull
    private String name;

    private Integer seq;


    private LocalDate plannedStartDate;

    @NotNull
    private LocalDate plannedEndDate;

    @ManyToOne
    private Project project;

    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    @JsonIgnore
    @Embedded
    @Delegate
    private ExecutableDelegate executableDelegate = new ExecutableDelegate();

    public Period getPeriod() {
        return Period.between(plannedStartDate, plannedEndDate);
    }

    public void move(Period offset) {
        this.plannedStartDate = this.plannedStartDate.plus(offset);
        this.plannedEndDate = this.plannedEndDate.plus(offset);
    }
}
