package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import androidx.databinding.ObservableField;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.TaskContentDialogBinding;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;

public class TaskDialogContent extends BaseContent implements OnClickListener,
        Executor.OnStatusChangeListener, OnProgressChange {
    private ObservableField<String> mNotify=new ObservableField<>();
    private ObservableField<String> mTitle=new ObservableField<>();
    private ObservableField<String> mMessage=new ObservableField<>();
    private int mAutoDismiss;
    private final ObservableField<ViewBinding> mConfirmBinding=new ObservableField<>();
    private final ObservableField<ConfirmResult.Confirm> mConfirm=new ObservableField<>();
    private final ObservableField<DoingFiles> mDoingFiles=new ObservableField<>();
    private final ObservableField<Result> mResult=new ObservableField<>();

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
            case R.string.succeed:
            case R.string.cancel:
                return removeFromParent()||true;
            case R.string.confirm:
                return makeConfirm(true)||true;
        }
        return false;
    }

    @Override
    public void onProgressChanged(Task task, Progress progress) {
        if (null!=progress){
            Object object=progress.getData();
            if (null!=object&&object instanceof DoingFiles){
                mDoingFiles.set((DoingFiles) object);
            }
        }
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        setConfirm(null);//Clean
        switch (status){
            case Executor.STATUS_FINISH:
                Result result=null!=task?task.getResult():null;
                result=null!=result?result:new Response<>(Code.CODE_UNKNOWN,"Unknown error.");
                int autoDismiss=mAutoDismiss;
                if (result instanceof ConfirmResult){
                    setConfirm(((ConfirmResult)result).make(getContext()));
                    return;
                }else if (autoDismiss>0){
                    post(()->removeFromParent(),autoDismiss>10000?10000:autoDismiss);//Auto dismiss
                }
                mResult.set(result);
//                setNotify(getString(result.isSucceed()?R.string.succeed:R.string.fail));
                break;
        }
    }

    public final TaskDialogContent setNotify(String notify){
        if (!isUiThread()){
            post(()->setNotify(notify));
            return this;
        }
        mNotify.set(notify);
        return this;
    }

    public final TaskDialogContent setTitle(String title){
        if (!isUiThread()){
            post(()->setTitle(title));
            return this;
        }
        mTitle.set(title);
        return this;
    }

    public final TaskDialogContent setMessage(String message){
        if (!isUiThread()){
            post(()->setMessage(message));
            return this;
        }
        mMessage.set(message);
        return this;
    }

    public TaskDialogContent setAutoDismiss(int autoDismiss) {
        this.mAutoDismiss = autoDismiss;
        return this;
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

    public ObservableField<DoingFiles> getDoingFiles() {
        return mDoingFiles;
    }

    public ObservableField<Result> getResult() {
        return mResult;
    }
}
