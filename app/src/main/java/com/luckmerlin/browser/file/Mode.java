package com.luckmerlin.browser.file;

import com.luckmerlin.browser.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Mode {
    public final static int MODE_MULTI_CHOOSE= R.string.multiChoose;
    public final static int MODE_DOWNLOAD=R.string.download;
    public final static int MODE_UPLOAD=R.string.upload;
    public final static int MODE_MOVE=R.string.move;
    public final static int MODE_COPY=R.string.copy;
    private ArrayList mArgs;
    private final int mMode;
    private Map<String,String> mExtra;

    public Mode(int mode){
        this(mode,null);
    }

    public Mode(int mode, ArrayList<File> args){
        mMode=mode;
        mArgs=args;
    }

    public Mode add(Object arg){
        if (null!=arg){
            ArrayList<Object> args=mArgs;
            args=null!=args?args:(mArgs=new ArrayList<>());
            synchronized (args){
                if (!args.contains(arg)){
                    args.add(arg);
                }
            }
        }
        return this;
    }

    public Mode cleanArgs(){
        ArrayList<File> args=mArgs;
        if (null!=args){
            args.clear();
            mArgs=null;
        }
        return this;
    }

    public Mode setExtra(Map<String,String> extra){
        mExtra=extra;
        return this;
    }

    public boolean isExistExtra(String key,String value){
        String data=getExtra(key,null);
        return (null==value&&null==data)||(null!=data&&null!=value&&data.equals(value));
    }

    public Mode setExtra(String key,String value){
        if (null!=key){
            Map<String,String> extra=mExtra;
            if (null==value){
                if (null!=extra){
                    extra.remove(key);
                    if (extra.size()<=0){
                        mExtra=null;
                    }
                }
            }else{
                extra=null!=extra?extra:(mExtra=new HashMap<>());
                extra.put(key,value);
            }
        }
        return this;
    }

    public final boolean isMode(int... modes){
        for (int child:modes) {
            if (child==mMode){
                return true;
            }
        }
        return false;
    }

    public Map<String, String> getExtra() {
        return mExtra;
    }

    public String getExtra(String key,String def) {
        Map<String,String> extra=mExtra;
        return null!=extra&&null!=key?extra.get(key):def;
    }

    public ArrayList getArgs() {
        return mArgs;
    }

    public int getMode() {
        return mMode;
    }
}
