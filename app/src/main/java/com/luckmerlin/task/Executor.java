package com.luckmerlin.task;

import com.luckmerlin.core.Matcher;

public interface Executor {
    boolean execute(Task task,OnProgressChange callback);
    void match(Matcher<TaskExecutor.ExecuteTask> matcher);
}
