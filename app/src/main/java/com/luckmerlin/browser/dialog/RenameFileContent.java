package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import androidx.databinding.ObservableField;

import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.CreateFileContentDialogBinding;
import com.luckmerlin.browser.databinding.RenameFileContentBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.click.OnClickListener;

public class RenameFileContent extends BaseContent implements OnClickListener {
    public final ObservableField<String> mInputName=new ObservableField<>();

    @Override
    protected View onCreateContent(Context context) {
        RenameFileContentBinding binding=inflate(context,R.layout.rename_file_content);
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
                return removeFromParent()||true;
        }
        return false;
    }

}
