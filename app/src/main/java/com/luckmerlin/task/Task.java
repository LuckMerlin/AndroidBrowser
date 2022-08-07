package com.luckmerlin.task;

public interface Task<A,R> {
    R execute(A arg,OnProgressChange callback);
    String getName();
}
