package com.luckmerlin.task;

public interface OnExecutePending extends TaskLifeCycle {
    boolean onExecutePending(TaskExecutor executor);
}
