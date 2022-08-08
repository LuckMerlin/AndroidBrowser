package com.luckmerlin.browser;

import com.luckmerlin.task.Progress;

public class Utils {

    public static int progress(Progress progress){
        return null!=progress?progress(progress.getPosition(),progress.getTotal()):0;
    }

    public static int progress(long current,long total){
        return total>0&&current>=0?(int)(current*100.f/total):0;
    }
}
