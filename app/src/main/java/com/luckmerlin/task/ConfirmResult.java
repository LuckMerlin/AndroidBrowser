package com.luckmerlin.task;

import android.content.Context;
import com.luckmerlin.core.Result;

public abstract class ConfirmResult implements Result {
    private Confirm mConfirm;

    public interface OnConfirmFinish{
        void onConfirmFinish(boolean confirm);
    }

    public class Confirm{
        private String mMessage;
        private OnConfirmFinish mOnConfirmFinish;

        public Confirm(){
            this(null,null);
        }

        public Confirm(String message,OnConfirmFinish callback) {
            setMessage(message).setOnConfirmFinish(callback);
        }

        public final String getMessage() {
            return mMessage;
        }

        public final OnConfirmFinish getOnConfirmFinish() {
            return mOnConfirmFinish;
        }

        public final Confirm setMessage(String message) {
            mMessage = message;
            return this;
        }

        public final Confirm setOnConfirmFinish(OnConfirmFinish callback) {
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
