package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.ConfirmContentDialogBinding;
import com.luckmerlin.task.ConfirmResult;

public class ConfirmDialogContent extends BaseContent {
    private ConfirmResult.Confirm mConfirm;

    public ConfirmDialogContent(ConfirmResult.Confirm confirmResult){
        mConfirm=confirmResult;
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

    public final ConfirmResult.Confirm getConfirm() {
        return mConfirm;
    }
}
