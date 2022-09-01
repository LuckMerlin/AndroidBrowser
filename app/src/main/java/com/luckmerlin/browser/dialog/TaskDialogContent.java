package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import androidx.databinding.ObservableField;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.TaskContentDialogBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Task;

public class TaskDialogContent extends BaseContent implements OnClickListener, Executor.OnStatusChangeListener {
    private ObservableField<String> mNotify=new ObservableField<>();
    private ObservableField<String> mTitle=new ObservableField<>();
    private ObservableField<String> mMessage=new ObservableField<>();
    private final ObservableField<ViewBinding> mConfirmBinding=new ObservableField<>();
    private final ObservableField<ConfirmResult.Confirm> mConfirm=new ObservableField<>();

    public final TaskDialogContent setNotify(String notify){
        mNotify.set(notify);
        return this;
    }

    @Override
    protected View onCreateContent(Context context) {
        TaskContentDialogBinding binding=inflate(context, R.layout.task_content_dialog);
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
                return removeFromParent()||true;
            case R.string.confirm:
                return (makeConfirm(true)&&removeFromParent())||true;
        }
        return false;
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        switch (status){
            case Executor.STATUS_FINISH:
                Result result=null!=task?task.getResult():null;
                ConfirmResult.Confirm confirm=null!=result&&result instanceof ConfirmResult?((ConfirmResult)result).make(getContext()):null;
                setConfirm(confirm);
                break;
        }
    }

    public TaskDialogContent setConfirm(ConfirmResult.Confirm confirm){
        mConfirm.set(confirm);
        return this;
    }

    private final boolean makeConfirm(boolean confirmed){
        ConfirmResult.Confirm confirm=mConfirm.get();
        ConfirmResult.OnConfirm onConfirm=null!=confirm?confirm.getOnConfirm():null;
        if (null==onConfirm){
            return true;
        }
        ViewBinding nextBinding=null!=onConfirm?onConfirm.onConfirm(confirmed):null;
        mConfirmBinding.set(nextBinding);
        return true;
    }

    public final ObservableField<String> getNotify() {
        return mNotify;
    }

    public ObservableField<String> getMessage() {
        return mMessage;
    }

    public ObservableField<String> getTitle() {
        return mTitle;
    }

    public final ObservableField<ConfirmResult.Confirm> getConfirm() {
        return mConfirm;
    }

    public final ObservableField<ViewBinding> getConfirmBinding() {
        return mConfirmBinding;
    }
}
