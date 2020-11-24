package tech.kuiperbelt.spm.domain.core.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;
import tech.kuiperbelt.spm.domain.core.RunningStatus;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;

@Getter
@Setter
@Embeddable
@MappedSuperclass
public class ExecutableDelegate implements ExecutableEntity {

    @Enumerated(EnumType.STRING)
    private RunningStatus status = RunningStatus.INIT;

    private boolean cancelled;

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

    @Override
    public void start() {
        Assert.isTrue(this.getStatus() == RunningStatus.INIT, "Only INIT project can be started");
        this.setStatus(RunningStatus.RUNNING);
        this.setActualStartDate(LocalDate.now());
    }

    @Override
    public boolean isCanBeStarted() {
        return this.getStatus() == RunningStatus.INIT;
    }

    @Override
    public void done() {
        Assert.isTrue( this.isCanBeDone(),
                "Only RUNNING project can be done");
        this.setStatus(RunningStatus.STOP);
        this.setActualEndDate(LocalDate.now());
    }

    @Override
    public boolean isCanBeDone() {
        return this.getStatus() == RunningStatus.RUNNING;
    }

    @Override
    public void cancel() {
        Assert.isTrue(this.getStatus() != RunningStatus.STOP, "STOP project can not be cancelled");
        this.setStatus(RunningStatus.STOP);
        this.setCancelled(true);
    }

    @Override
    public boolean isCanBeCancelled() {
        return this.getStatus() != RunningStatus.STOP;
    }

    @Override
    public boolean isCanBeDeleted() {
        return RunningStatus.RUNNING != getStatus();
    }
}
