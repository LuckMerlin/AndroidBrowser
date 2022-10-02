package com.luckmerlin.task;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.core.Brief;
import com.luckmerlin.core.Result;

public class Confirm implements Brief, Result {
    private String mMessage;
    private String mTitle;
    private Binding mBinding;

    public final String getMessage() {
        return mMessage;
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
