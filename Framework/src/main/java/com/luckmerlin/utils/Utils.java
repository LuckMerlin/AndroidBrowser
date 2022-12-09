package com.luckmerlin.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Looper;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static String getString(Context context,int resId,String def, Object... formatArgs){
        return null!=context?context.getString(resId,formatArgs):def;
    }


    public static CharSequence getText(Context context,int resId,CharSequence def){
        return null!=context?context.getText(resId):def;
    }


    public static boolean isLandscape(Resources resources){
        return isOrientation(resources,Configuration.ORIENTATION_LANDSCAPE);
    }

    public static boolean isOrientation(Resources resources,int orientation){
        Configuration configuration=null!=resources?resources.getConfiguration():null;
        return null!=configuration&&configuration.orientation==orientation;
    }

    public static boolean isEqualed(Object arg1,Object arg2,boolean ignoreNull){
        return (null!=arg1&&null!=arg2&&arg1.equals(arg2))&&(ignoreNull?false:(null==arg1&&null==arg2));
    }

    public static boolean isUiThread(){
        Looper looper=Looper.myLooper();
        Looper uiLooper=Looper.getMainLooper();
        return null!=looper&&null!=uiLooper&&looper==uiLooper;
    }

    public static String formatSizeText(Object fileSize){
        if (null==fileSize){
            return null;
        }else if (fileSize instanceof Double){
            return formatSizeText((double)((Double)fileSize));
        }else if (fileSize instanceof Long){
            return formatSizeText((double)((Long)fileSize));
        }else if (fileSize instanceof Integer){
            return formatSizeText((double)((Integer)fileSize));
        }else if (fileSize instanceof Short){
            return formatSizeText((double)((Short)fileSize));
        }
        return null;
    }


    public static String formatSizeText(double fileSize){
        fileSize=fileSize<=0?0:fileSize;
        double kiloByte = fileSize/1024;
        if(kiloByte < 1) {
            return fileSize + "B";
        }
        double megaByte = kiloByte/1024;
        if(megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "K";
        }
        double gigaByte = megaByte/1024;
        if(gigaByte < 1) {
            BigDecimal result2  = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "M";
        }
        double teraBytes = gigaByte/1024;
        if(teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "G";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "T";
    }

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
