package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DialogRenameFileBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.view.ViewContent;

public abstract class RenameFileContent extends DialogContent {
    private final ObservableField<String> mInputName=new ObservableField<>();
    private final ObservableField<Boolean> mKeepPostfix=new ObservableField<>();

    public RenameFileContent(){
        setDialogContent(new ViewContent(){
            @Override
            protected View onCreateContent(Context context) {
                DialogRenameFileBinding binding=inflate(context, R.layout.dialog_rename_file);
                binding.setContent(RenameFileContent.this);
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
                    Boolean keepPostfix=mKeepPostfix.get();
                    return onRenameFile(inputName,null!=keepPostfix&&keepPostfix);
                }), ViewBinding.clickId(R.string.cancel)));
    }

    protected abstract boolean onRenameFile(String inputName,boolean createDir);

    @Override
    protected View onCreateContent(Context context) {
        setTitle(getString(context,R.string.rename));
        return super.onCreateContent(context);
    }

    public ObservableField<String> getInputName() {
        return mInputName;
    }

    public ObservableField<Boolean> isKeepPostfix() {
        return mKeepPostfix;
    }
}
