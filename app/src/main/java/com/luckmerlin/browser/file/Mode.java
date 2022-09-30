package com.luckmerlin.browser.file;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.BrowserActivityModel;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.click.Listener;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.OnConfirm;
import com.luckmerlin.core.OnFinish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Mode {
    public final static int MODE_MULTI_CHOOSE= R.string.multiChoose;
    public final static int MODE_DOWNLOAD=R.string.download;
    public final static int MODE_UPLOAD=R.string.upload;
    public final static int MODE_MOVE=R.string.move;
    public final static int MODE_COPY=R.string.copy;
    public final static int MODE_DELETE=R.string.delete;
    private ArrayList mArgs;
    private final int mMode;
    private Map<String,String> mExtra;
    private boolean mAllEnabled;
    private OnConfirm<Object,Boolean> mOnConfirm;
    private Binding mBinding;

    public Mode(int mode){
        this(mode,null);
    }

    public Mode(int mode, ArrayList<File> args){
        mMode=mode;
        mArgs=args;
    }

    public final Mode setOnConfirm(OnConfirm<Object,Boolean> onConfirm) {
        this.mOnConfirm = onConfirm;
        return this;
    }

    public OnConfirm<Object, Boolean> getOnConfirm() {
        return mOnConfirm;
    }

    public Mode makeSureBinding(Listener listener){
        mBinding= new DialogButtonBinding(ViewBinding.clickId(R.string.sure).setListener(listener));
        return this;
    }

    public boolean isContains(Object arg){
        ArrayList<Object> args=null!=arg?mArgs:null;
        if (null!=args){
            synchronized (args){
               return args.contains(arg);
            }
        }
        return false;
    }

    public Mode remove(Object arg){
        ArrayList<Object> args=null!=arg?mArgs:null;
        if (null!=args){
            synchronized (args){
                args.remove(arg);
            }
        }
        return this;
    }

    public Mode addFile(Object arg){
        return (null==arg||!(arg instanceof File))?this:add(arg);
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

    public boolean checkArgs(Matcher<Object> matcher){
        ArrayList<Object> args=mArgs;
        if (null!=args&&null!=matcher){
            synchronized (args){
                Boolean matched=null;
                for (Object child:args) {
                    if (null==(matched=matcher.match(child))||!matched){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
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

    public boolean enableAll(boolean enable){
        if (mAllEnabled!=enable){
            mAllEnabled=enable;
            return true;
        }
        return false;
    }

    public final boolean isAllEnabled(){
        return mAllEnabled;
    }

    public final boolean isMode(int... modes){
        Integer current=mMode;
        if (null!=current&&null!=modes){
            for (int child:modes) {
                if (child==current){
                    return true;
                }
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

    public Mode setBinding(Binding binding){
        mBinding=binding;
        return this;
    }

    public Binding getBinding() {
        return mBinding;
    }
}
