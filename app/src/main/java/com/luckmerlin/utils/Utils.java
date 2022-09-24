package com.luckmerlin.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.security.ConfirmationCallback;

import com.luckmerlin.task.Progress;

import java.io.Closeable;
import java.io.IOException;

public class Utils {

    public static int progress(Progress progress){
        return null!=progress?progress(progress.getPosition(),progress.getTotal()):0;
    }

    public static int progress(long current,long total){
        return total>0&&current>=0?(int)(current*100.f/total):0;
    }

    public final static void closeStream(Closeable... closeables){
        if (null!=closeables){
            for (Closeable child:closeables) {
                if (null!=child){
                    try {
                        child.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static boolean isLandscape(Resources resources){
        return isOrientation(resources,Configuration.ORIENTATION_LANDSCAPE);
    }

    public static boolean isOrientation(Resources resources,int orientation){
        Configuration configuration=null!=resources?resources.getConfiguration():null;
        return null!=configuration&&configuration.orientation==orientation;
    }
}
