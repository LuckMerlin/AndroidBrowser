package com.luckmerlin.task;

import com.luckmerlin.core.Result;

public interface Task{
    Result execute(Runtime runtime,OnProgressChange callback);
    String getName();
    Progress getProgress();
    Result getResult();
    default Doing getDoing(){
        return null;
    }
}
