package com.luckmerlin.task;

public interface OnExecuteWaiting extends TaskLifeCycle {
    boolean onExecuteWaiting(TaskExecutor executor);
}
