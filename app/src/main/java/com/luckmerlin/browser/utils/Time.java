package com.luckmerlin.browser.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
@Deprecated
public class Time {

    public static String formatMediaDuration(long mills){
            long hours=mills/(1000*60*60);
            long wholeHours=hours*(1000 * 60 * 60 );
            long minutes = (mills-wholeHours)/(1000* 60);
            long seconds= (mills-wholeHours-(minutes*1000*60))/1000;
            return String.format("%02d", hours)+":"+ String.format("%02d", minutes)+":"+
                    String.format("%02d", seconds);
    }

    public static String formatTime(long mills){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(mills));
    }
}
