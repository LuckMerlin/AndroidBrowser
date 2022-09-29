package com.luckmerlin.task;

import android.content.Context;
import android.view.View;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.OnViewBinding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.core.Brief;
import com.luckmerlin.core.Result;

public abstract class ConfirmResult implements Result {
    private Confirm mConfirm;

    public interface OnConfirm{
        ViewBinding onConfirm(boolean confirm);
    }

    public static class Confirm implements Brief {
        private String mMessage;
        private String mTitle;
        private Binding mBinding;

        @Deprecated
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

        @Deprecated
        public final OnConfirm getOnConfirm() {
            return mOnConfirmFinish;
        }

        public final Confirm setMessage(String message) {
            mMessage = message;
            return this;
        }

        public final Confirm setBinding(Binding binding) {
            mBinding = binding;
            return this;
        }

        public final Confirm setTitle(String title) {
            mTitle = title;
            return this;
        }

        public final String getTitle() {
            return mTitle;
        }

        @Deprecated
        public final Confirm setOnConfirm(OnConfirm callback) {
            mOnConfirmFinish = callback;
            return this;
        }

        @Override
        public CharSequence getNote() {
            return mMessage;
        }

        @Override
        public CharSequence getName() {
            return mTitle;
        }

        @Override
        public Object getIcon() {
            return null;
        }

        public final Binding getBinding() {
            return mBinding;
        }
    }

    protected abstract Confirm onCreate(Context context);

    public final Confirm make(Context context){
        Confirm confirm=mConfirm;
        return null!=confirm?confirm:null!=context?mConfirm=onCreate(context):null;
    }
}
