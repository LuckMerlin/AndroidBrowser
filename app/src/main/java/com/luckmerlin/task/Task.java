package com.luckmerlin.task;

import com.luckmerlin.core.Result;

public interface Task{
    Result execute(OnProgressChange callback);
    String getName();
    Progress getProgress();
    Result getResult();
    boolean isPending();
}
