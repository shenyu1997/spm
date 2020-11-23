package tech.kuiperbelt.spm.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.support.AuditDelegate;
import tech.kuiperbelt.spm.domain.core.support.AuditListener;
import tech.kuiperbelt.spm.domain.core.support.AuditableEntity;
import tech.kuiperbelt.spm.domain.core.support.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
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
@Table(name = "phases", uniqueConstraints={@UniqueConstraint(columnNames={"id","seq"})})
public class Phase extends BaseEntity implements AuditableEntity, ExecutableEntity {
    @ToString.Include
    @NotNull
    private String name;

    private Integer seq;

    private LocalDate plannedStartDate;

    @NotNull
    private LocalDate plannedEndDate;

    @ManyToOne
    private Project project;

    @Builder.Default
    @RestResource(path = "work-items")
    @OneToMany(mappedBy = WorkItem.Fields.phase)
    private List<WorkItem> workItems = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate
    private AuditDelegate auditDelegate = new AuditDelegate();

    @Builder.Default
    @JsonIgnore
    @Embedded
    @Delegate(excludes = PhaseExecutableExclude.class)
    private ExecutableDelegate executableDelegate = new ExecutableDelegate();

    public Period getPeriod() {
        if(getPlannedStartDate() != null && getPlannedEndDate() != null) {
            return Period.between(getPlannedStartDate(), getPlannedEndDate());
        } else {
            return null;
        }
    }

    public void move(Period offset) {
        setPlannedStartDate(getPlannedStartDate().plus(offset));
        setPlannedEndDate(getPlannedEndDate().plus(offset));
    }

    @Builder.Default
    @JsonIgnore
    private boolean allItemStop = true;

    public void checkAllItemsStop() {
        setAllItemStop(getWorkItems().stream()
                .allMatch(phase -> phase.getStatus() == RunningStatus.STOP));
    }

    @Override
    public boolean isCanBeDone() {
        return isAllItemStop() && executableDelegate.isCanBeDone();
    }

    @Override
    public void done() {
        Assert.isTrue(isAllItemStop(), "all workItems stop");
        executableDelegate.done();
    }

    private interface PhaseExecutableExclude {
        void isCanBeDone();
        void done();
    }

    public boolean isOverflowBy(LocalDate timeFrame) {
        return timeFrame != null && timeFrame.isAfter(this.getPlannedEndDate());
    }
}
