package com.luckmerlin.task;

import android.content.Context;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.core.Result;

public abstract class ConfirmResult implements Result {
    private Confirm mConfirm;

    public interface OnConfirm{
        ViewBinding onConfirm(boolean confirm);
    }

    public static class Confirm{
        private String mMessage;
        private String mTitle;
        private OnConfirm mOnConfirmFinish;

        public Confirm(){
            this(null,null);
        }

        public Confirm(String message,OnConfirm callback) {
            setMessage(message).setOnConfirm(callback);
        }

        public final String getMessage() {
            return mMessage;
        }

        public final OnConfirm getOnConfirm() {
            return mOnConfirmFinish;
        }

        public final Confirm setMessage(String message) {
            mMessage = message;
            return this;
        }

        public final Confirm setTitle(String title) {
            mTitle = title;
            return this;
        }

        public final String getTitle() {
            return mTitle;
        }

        public final Confirm setOnConfirm(OnConfirm callback) {
            mOnConfirmFinish = callback;
            return this;
        }
    }

    protected abstract Confirm onCreate(Context context);

    public final Confirm make(Context context){
        Confirm confirm=mConfirm;
        return null!=confirm?confirm:null!=context?mConfirm=onCreate(context):null;
    }
}
