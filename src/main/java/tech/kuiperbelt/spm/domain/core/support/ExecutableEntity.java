package tech.kuiperbelt.spm.domain.core.support;

import tech.kuiperbelt.spm.domain.core.RunningStatus;

public interface ExecutableEntity {
    RunningStatus getStatus();

    boolean isCanBeStarted();

    void start();

    boolean isCanBeDone();

    void done();

    boolean isCanBeCancelled();

    void cancel();

    boolean isCancelled();

    boolean isCanBeDeleted();
}
