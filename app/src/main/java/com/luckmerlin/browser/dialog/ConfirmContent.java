package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import androidx.databinding.ObservableField;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.ConfirmContentBinding;
import com.luckmerlin.task.Confirm;

public class ConfirmContent extends BaseContent {
    private final ObservableField<Confirm> mConfirm=new ObservableField<>();

    @Override
    protected View onCreateContent(Context context) {
        ConfirmContentBinding binding=inflate(context, R.layout.confirm_content);
        if (null!=binding){
            binding.setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    public final ConfirmContent setConfirm(Confirm confirm){
        mConfirm.set(confirm);
        return this;
    }

    public final ObservableField<Confirm> getConfirm() {
        return mConfirm;
    }
}
