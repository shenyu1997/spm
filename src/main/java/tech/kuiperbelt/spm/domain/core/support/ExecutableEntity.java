package tech.kuiperbelt.spm.domain.core.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.RunningStatus;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.util.EnumSet;

@Getter
@FieldNameConstants
@Embeddable
@MappedSuperclass
public class ExecutableEntity extends BaseEntity {

    @Audited
    @Enumerated(EnumType.STRING)
    private RunningStatus status;

    @Audited
    private boolean cancelled;

    @Audited
    private LocalDate actualStartDate;

    @Audited
    private LocalDate actualEndDate;

    public void initStatus() {
        this.status = RunningStatus.INIT;
    }


    public void start() {
        Assert.isTrue(this.getStatus() == RunningStatus.INIT, "Only INIT status can be started");
        this.setStatus(RunningStatus.RUNNING);
        this.setActualStartDate(LocalDate.now());
    }

    @JsonIgnore
    public boolean isCanBeStarted() {
        return this.getStatus() == RunningStatus.INIT;
    }

    public void done() {
        Assert.isTrue( this.isCanBeDone(),
                "Only RUNNING project can be done");
        this.setStatus(RunningStatus.STOP);
        this.setActualEndDate(LocalDate.now());
    }

    @JsonIgnore
    public boolean isCanBeDone() {
        return this.getStatus() == RunningStatus.RUNNING;
    }

    public void cancel() {
        Assert.isTrue(this.getStatus() != RunningStatus.STOP, "STOP project can not be cancelled");
        this.setStatus(RunningStatus.STOP);
        this.setCancelled(true);
    }

    @JsonIgnore
    public boolean isCanBeCancelled() {
        return this.getStatus() != RunningStatus.STOP;
    }

    @JsonIgnore
    public boolean isCanBeDeleted() {
        return RunningStatus.RUNNING != getStatus();
    }


    private void setStatus(RunningStatus status) {
        this.status = status;
    }

    private void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private void setActualStartDate(LocalDate actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    private void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public EnumSet<Action> getActions() {
        EnumSet<Action> result = EnumSet.noneOf(Action.class);
        if(isCanBeStarted()) {
            result.add(Action.start);
        }
        if(isCanBeDone()) {
            result.add(Action.done);
        }
        if(isCanBeCancelled()) {
            result.add(Action.cancel);
        }
        if(isCanBeDeleted()) {
            result.add(Action.delete);
        }
        return result;
    }

    public enum Action {
        start, done, cancel, delete
    }
}
