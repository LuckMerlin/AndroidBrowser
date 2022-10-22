package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import androidx.databinding.ObservableField;

import com.luckmerlin.binding.DataBindingUtil;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.FileContentMenusBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.ViewContent;

public class FileContextDialogContent extends ViewContent implements OnClickListener {
    private final ObservableField<String> mTitle=new ObservableField<>();
    private final File mFile;

    public FileContextDialogContent(File file){
        mFile=file;
    }

    public FileContextDialogContent setTitle(String title){
        mTitle.set(title);
        return this;
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        removeFromParent();//Anyone view click to dismiss
        switch (clickId){
            case R.layout.browser_content_menus:
                Debug.D("WERWERQWEQ  "+clickId);
                return true;
        }
        return false;
    }

    public final File getFile() {
        return mFile;
    }

    @Override
    protected View onCreateContent(Context context) {
        FileContentMenusBinding binding= DataBindingUtil.inflate(context, R.layout.file_content_menus);
        if (null!=binding){
            binding.setContent(this);
            return binding.getRoot();
        }
        return null;
    }

    public final ObservableField<String> getTitle() {
        return mTitle;
    }
}
