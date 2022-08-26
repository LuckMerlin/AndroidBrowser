package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import androidx.databinding.ObservableField;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.ConfirmContentDialogBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.task.ConfirmResult;

public class ConfirmDialogContent extends BaseContent implements OnClickListener {
    private ConfirmResult.Confirm mConfirm;
    private OnConfirmFinish mOnConfirmFinish;
    private final ObservableField<ViewBinding> mConfirmBinding=new ObservableField<>();

    public interface OnConfirmFinish{
        Object onConfirmFinish(boolean confirmed,Object confirmObj);
    }

    public ConfirmDialogContent(ConfirmResult.Confirm confirmResult){
        mConfirm=confirmResult;
    }

    public final ConfirmDialogContent setOnConfirmFinish(OnConfirmFinish onConfirmFinish){
            mOnConfirmFinish=onConfirmFinish;
            return this;
    }

    protected void onConfirmFinish(boolean confirmed,Object confirmObj){
        //Do nothing
    }

    @Override
    protected View onCreateContent(Context context) {
        ConfirmContentDialogBinding binding=inflate(context, R.layout.confirm_content_dialog);
        if (null!=binding){
            binding.setContent(this);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.drawable.selector_close:
            case R.string.cancel:
                return (makeConfirm(false)&&removeFromParent())||true;
            case R.string.confirm:
                return (makeConfirm(true)&&removeFromParent())||true;
        }
        return false;
    }

    private final boolean makeConfirm(boolean confirmed){
        ConfirmResult.Confirm confirm=mConfirm;
        ConfirmResult.OnConfirm onConfirm=null!=confirm?confirm.getOnConfirm():null;
        if (null==onConfirm){
            return true;
        }
        Object obj=null!=onConfirm?onConfirm.onConfirm(confirmed):null;
        onConfirmFinish(confirmed,obj);
        OnConfirmFinish onConfirmFinish=mOnConfirmFinish;
        Object nextObj=null!=onConfirmFinish?onConfirmFinish.onConfirmFinish(confirmed,obj):null;
        if (null==nextObj||!(nextObj instanceof ViewBinding)){
            return true;
        }
        mConfirmBinding.set((ViewBinding) nextObj);
        return false;
    }

    public final ConfirmResult.Confirm getConfirm() {
        return mConfirm;
    }

    public final ObservableField<ViewBinding> getConfirmBinding() {
        return mConfirmBinding;
    }
}
