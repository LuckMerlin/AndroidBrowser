package com.luckmerlin.task;

import com.luckmerlin.core.Result;

public interface Task{
    Result execute(Runtime runtime,OnProgressChange callback);
    String getName();
    Ongoing getOngoing();
    Result getResult();
}
