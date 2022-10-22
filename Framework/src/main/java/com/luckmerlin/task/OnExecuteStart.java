package com.luckmerlin.task;

public interface OnExecuteStart extends TaskLifeCycle {
    boolean onExecuteStart(TaskExecutor executor);
}
