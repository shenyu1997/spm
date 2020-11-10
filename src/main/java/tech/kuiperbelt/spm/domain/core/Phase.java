package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.hibernate.envers.Audited;
import tech.kuiperbelt.spm.common.AuditDelegate;
import tech.kuiperbelt.spm.common.AuditListener;
import tech.kuiperbelt.spm.common.AuditableEntity;
import tech.kuiperbelt.spm.common.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


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

    private int seq;

    @Enumerated(EnumType.STRING)
    private RunningStatus status;

    private boolean cancelled;

    private LocalDate plannedStartDate;

    @NotNull
    private LocalDate plannedEndDate;

    @ManyToOne
    private Project project;


    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();
}
