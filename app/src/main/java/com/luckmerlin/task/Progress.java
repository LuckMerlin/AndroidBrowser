package com.luckmerlin.task;

public interface Progress {
    long getTotal();
    long getPosition();

    default int intValue(){
        long total=getTotal();
        long pos=getPosition();
        return pos>=0&&total>0?(int)(pos*100.f/total):0;
    }
}
