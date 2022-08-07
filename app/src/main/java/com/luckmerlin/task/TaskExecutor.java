package com.luckmerlin.task;

import com.luckmerlin.core.Group;

public interface TaskExecutor<T extends Task> extends Group<T> {
    T getExecuting();
}
