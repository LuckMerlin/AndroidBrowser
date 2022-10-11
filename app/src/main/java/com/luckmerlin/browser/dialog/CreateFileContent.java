package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DialogCreateFileBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.view.ViewContent;

public abstract class CreateFileContent extends DialogContent {
    private final ObservableField<String> mInputName=new ObservableField<>();
    private final ObservableField<Boolean> mCreateDir=new ObservableField<>();

    public CreateFileContent(){
        setDialogContent(new ViewContent(){
            @Override
            protected View onCreateContent(Context context) {
                DialogCreateFileBinding binding=inflate(context, R.layout.dialog_create_file);
                binding.setContent(CreateFileContent.this);
                return binding.getRoot();
            }
        });
        setButtonBinding(new DialogButtonBinding(ViewBinding.clickId(R.string.sure).
                setListener((OnClickListener)(View view, int clickId, int count, Object obj)-> {
                    String inputName=mInputName.get();
                    inputName=null!=inputName?inputName.trim():null;
                    if (null==inputName||inputName.length()<=0){
                        return toast(getString(R.string.inputEmpty))||true;
                    }
                    Boolean createDirBool=mCreateDir.get();
                    return onCreateFile(inputName,null!=createDirBool&&createDirBool);
                }), ViewBinding.clickId(R.string.cancel)));
    }

    protected abstract boolean onCreateFile(String inputName,boolean createDir);

    @Override
    protected View onCreateContent(Context context) {
        setTitle(getString(context,R.string.createFile));
        return super.onCreateContent(context);
    }

    public ObservableField<String> getInputName() {
        return mInputName;
    }

    public ObservableField<Boolean> isCreateDir() {
        return mCreateDir;
    }
}
