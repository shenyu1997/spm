package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import org.springframework.util.Assert;
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
@Table(name = "phases", uniqueConstraints={@UniqueConstraint(columnNames={"id","seq"})})
public class Phase extends BaseEntity implements AuditableEntity {
    @NotNull
    private String name;

    private Integer seq;

    @Enumerated(EnumType.STRING)
    private RunningStatus status;

    private LocalDate plannedStartDate;

    @NotNull
    private LocalDate plannedEndDate;

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

    @ManyToOne
    private Project project;

    public Period getPeriod() {
        return Period.between(plannedStartDate, plannedEndDate);
    }

    public void move(Period offset) {
        this.plannedStartDate = this.plannedStartDate.plus(offset);
        this.plannedEndDate = this.plannedEndDate.plus(offset);
    }

    public void start() {
        Assert.isTrue(this.getStatus() == RunningStatus.INIT, "Only INIT phase can be started");
        this.setStatus(RunningStatus.RUNNING);
        this.setActualStartDate(LocalDate.now());
    }

    public void done() {
        Assert.isTrue(this.getStatus() == RunningStatus.RUNNING, "Only RUNNING phase can be done");
        this.setStatus(RunningStatus.STOP);
        this.setActualEndDate(LocalDate.now());
    }


    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();
}
