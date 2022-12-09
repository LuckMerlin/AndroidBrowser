package com.luckmerlin.task;

import com.luckmerlin.core.Result;

public interface Task{
    Result execute(Runtime runtime,OnProgressChange callback);
    default String getName(){
        return null;
    }
    default Ongoing getOngoing(){
        return null;
    }

    default Result getResult(){
        return null;
    }
}
