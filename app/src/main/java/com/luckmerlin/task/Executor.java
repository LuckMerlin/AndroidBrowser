package com.luckmerlin.task;

import java.util.List;

public interface Executor {
    boolean execute(Task task,OnProgressChange callback);
    List<Task> getExecuting();
}
