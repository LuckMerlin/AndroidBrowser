package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DialogGoToFolderBinding;
import com.luckmerlin.browser.databinding.DialogRenameFileBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.view.ViewContent;

public abstract class GoToFolderContent extends DialogContent {
    private final ObservableField<String> mInputPath=new ObservableField<>();
    private final ObservableField<String> mHint=new ObservableField<>();

    public GoToFolderContent(){
        setDialogContent(new ViewContent(){
            @Override
            protected View onCreateContent(Context context) {
                DialogGoToFolderBinding binding=inflate(context, R.layout.dialog_go_to_folder);
                binding.setContent(GoToFolderContent.this);
                return binding.getRoot();
            }
        });
        setButtonBinding(new DialogButtonBinding(ViewBinding.clickId(R.string.goTo).
                setListener((OnClickListener)(View view, int clickId, int count, Object obj)-> {
                    String inputPath=mInputPath.get();
                    inputPath=null!=inputPath?inputPath.trim():null;
                    if (null==inputPath||inputPath.length()<=0){
                        return toast(getString(R.string.inputEmpty))||true;
                    }
                    return onGoToFolder(inputPath);
                }), ViewBinding.clickId(R.string.cancel)));
    }

    protected abstract boolean onGoToFolder(String inputPath);

    @Override
    protected View onCreateContent(Context context) {
        setTitle(getString(context,R.string.goTo));
        mHint.set(getString(context,R.string.inputWhich,getString(context,R.string.path)));
        return super.onCreateContent(context);
    }

    public ObservableField<String> getInputPath() {
        return mInputPath;
    }

    public ObservableField<String> getHint() {
        return mHint;
    }
}
