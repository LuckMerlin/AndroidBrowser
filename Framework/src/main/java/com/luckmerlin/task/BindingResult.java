package com.luckmerlin.task;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.core.MessageResult;

public class BindingResult implements MessageResult {
    private Binding mBinding;
    private boolean mSucceed=false;
    private CharSequence mMessage;

    public final BindingResult setBinding(Binding binding) {
        mBinding = binding;
        return this;
    }

    public final BindingResult setSucceed(boolean succeed) {
        mSucceed = succeed;
        return this;
    }

    public final BindingResult setMessage(CharSequence message){
        mMessage=message;
        return this;
    }

    @Override
    public final CharSequence getMessage() {
        return mMessage;
    }

    @Override
    public final boolean isSucceed() {
        return mSucceed;
    }

    public final Binding getBinding() {
        return mBinding;
    }

}
