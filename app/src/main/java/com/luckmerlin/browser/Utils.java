package com.luckmerlin.browser;

import java.io.Closeable;
import java.io.IOException;

public class Utils {
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
}
