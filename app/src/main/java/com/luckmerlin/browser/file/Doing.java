package com.luckmerlin.browser.file;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.core.Brief;
import com.luckmerlin.utils.Utils;

public class Doing<F extends Brief,T extends Brief> {
    private F mFrom;
    private T mTo;
    private int mProgress;
    private Integer mMode;
    private Binding mBinding;

    public Doing setFrom(F from){
        mFrom=from;
        return this;
    }

    public Doing setTo(T to){
        mTo=to;
        return this;
    }

    public Doing setSucceed(boolean succeed) {
        mProgress=succeed?100:0;
        return this;
    }

    public Doing setProgress(int progress) {
        this.mProgress = progress;
        return this;
    }

    public boolean isFinish(){
        return mProgress ==100;
    }

    public int getProgress() {
        return mProgress;
    }

    public boolean isFromToEquals(){
        F from=mFrom;
        T to=mTo;
        return (null==from&&null==to)||(null!=from&&null!=to&&from.equals(to));
    }

    public final Doing setDoingBinding(Binding binding) {
        this.mBinding = binding;
        return this;
    }

    public final Binding getDoingBinding() {
        return mBinding;
    }

    public Doing setDoingMode(Integer mode){
        mMode=mode;
        return this;
    }

    public boolean isDoingMode(int modeInt){
        Integer mode=mMode;
        return null!=mode&&mode== modeInt;
    }

    public CharSequence getTitle(){
        F from=mFrom;
        T to=mTo;
        return null!=from?from.getName():null!=to?to.getName():null;
    }

    public F getFrom() {
        return mFrom;
    }

    public T getTo() {
        return mTo;
    }

    @Override
    public boolean equals(Object o) {
        if (null==o||!(o instanceof Doing)){
            return false;
        }
        Doing doing=(Doing)o;
        return mProgress==doing.mProgress&&
               Utils.isEqualed(mFrom,doing.mFrom,false)&&
               Utils.isEqualed(mBinding,doing.mBinding,false)&&
               Utils.isEqualed(mTo,doing.mTo,false)&&
               Utils.isEqualed(mMode,doing.mMode,false);
    }
}
