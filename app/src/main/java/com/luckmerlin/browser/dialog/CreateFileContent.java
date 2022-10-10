package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DialogCreateFileBinding;

public class CreateFileContent extends BaseContent {
    private final ObservableField<String> mInputName=new ObservableField<>();
    private final ObservableField<Boolean> mCreateDir=new ObservableField<>();

    @Override
    protected View onCreateContent(Context context) {
        DialogCreateFileBinding binding=inflate(context, R.layout.dialog_create_file);
        binding.setContent(this);
        return binding.getRoot();
    }

    public ObservableField<String> getInputName() {
        return mInputName;
    }

    public ObservableField<Boolean> isCreateDir() {
        return mCreateDir;
    }
}
