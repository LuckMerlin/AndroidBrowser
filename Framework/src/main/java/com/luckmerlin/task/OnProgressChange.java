package com.luckmerlin.task;

public interface OnProgressChange extends TaskLifeCycle {
    void onProgressChanged(Task task,Progress progress);
}
