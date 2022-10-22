package com.luckmerlin.debug;

import android.util.Log;

public class Debug {
    private static String TAG="LM";

    public static void D(String tag,String msg){
        Log.d(null!=tag?tag:TAG,null!=msg?msg:"");
    }

    public static void D(String msg){
        D(null,msg);
    }

    public static void E(String tag,String msg,Throwable throwable){
        Log.e(null!=tag?tag:TAG,null!=msg?msg:"");
    }

    public static void E(String msg,Throwable throwable){
       E(null,msg,throwable);
    }

    public static void E(String tag,String msg){
        E(tag,msg,null);
    }

    public static void E(String msg){
        E(null,msg);
    }

    public static void W(String tag,String msg){
        Log.w(null!=tag?tag:TAG,null!=msg?msg:"");
    }

    public static void W(String msg){
        W(null,msg);
    }
}
