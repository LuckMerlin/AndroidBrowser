package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import androidx.databinding.ObservableField;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.CreateFileContentDialogBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.click.OnClickListener;

public abstract class CreateFileDialogContent extends BaseContent implements OnClickListener {
    public final ObservableField<Boolean> mCreateDir=new ObservableField<>();
    public final ObservableField<String> mInputName=new ObservableField<>();
    private File mParent;

    public CreateFileDialogContent(File parent){
        mParent=parent;
    }

    @Override
    protected View onCreateContent(Context context) {
        CreateFileContentDialogBinding binding=inflate(context,R.layout.create_file_content_dialog);
        if (null!=binding){
            binding.setContent(this);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.string.createFile:
            case R.string.createFolder:
                return create()&&(removeFromParent()||true);
            case R.drawable.selector_close:
                return removeFromParent()||true;
        }
        return false;
    }

    protected abstract boolean onCreate(File parent,String name,boolean createDir);

    private final boolean create(){
        String inputName=mInputName.get();
        inputName=null!=inputName?inputName.trim():null;
        if (null==inputName||inputName.length()<=0){
            return toast(getString(R.string.inputEmpty))&&false;
        }
        Boolean createDir=mCreateDir.get();
        return onCreate(mParent, inputName, null != createDir && createDir);
    }
}
