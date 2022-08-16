package com.luckmerlin.task;

import com.luckmerlin.core.Matcher;

public interface TaskSaver {
    void load(Matcher<Task> matcher);
    void save(Task task);
}
