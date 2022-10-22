package com.luckmerlin.task;

public interface OnExecuteFinish extends TaskLifeCycle {
    boolean onExecuteFinish(TaskExecutor executor);
}
