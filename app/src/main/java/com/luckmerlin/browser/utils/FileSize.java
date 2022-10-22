package com.luckmerlin.browser.utils;

import java.math.BigDecimal;

@Deprecated
public final class FileSize {

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
}
