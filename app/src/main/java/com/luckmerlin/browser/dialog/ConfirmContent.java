package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import androidx.databinding.ObservableField;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.ConfirmContentBinding;
import com.luckmerlin.task.Confirm;

public class ConfirmContent extends BaseContent {
    private final ObservableField<String> mTitle=new ObservableField<>();
    private final ObservableField<String> mMessage=new ObservableField<>();
    private final ObservableField<Confirm> mConfirm=new ObservableField<>();
    private final ObservableField<Binding> mBinding=new ObservableField<>();

    @Override
    protected View onCreateContent(Context context) {
        ConfirmContentBinding binding=inflate(context, R.layout.confirm_content);
        if (null!=binding){
            binding.setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    public final ConfirmContent setTitle(String name){
        mTitle.set(name);
        return this;
    }

    public final ConfirmContent setMessage(String msg){
        mMessage.set(msg);
        return this;
    }

    public final ConfirmContent setConfirmBinding(Binding binding){
        if (!isUiThread()){
            post(()->setConfirmBinding(binding));
            return this;
        }
        mBinding.set(binding);
        return this;
    }

    public final ObservableField<String> getTitle(){
        return mTitle;
    }

    public final ConfirmContent setConfirm(Confirm confirm){
        if (!isUiThread()){
            post(()->setConfirm(confirm));
            return this;
        }
        mConfirm.set(confirm);
        return this;
    }

    public final ObservableField<Confirm> getConfirm() {
        return mConfirm;
    }

    public ObservableField<String> getMessage() {
        return mMessage;
    }

    public final ObservableField<Binding> getBinding() {
        return mBinding;
    }


}
