package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import androidx.databinding.ObservableField;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DoingTaskBinding;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Brief;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.ConfirmResult1;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;

public class DoingContent extends ConfirmContent implements Executor.OnStatusChangeListener, OnProgressChange {
    private final ObservableField<String> mTitle=new ObservableField<>();
    private final ObservableField<Brief> mFrom=new ObservableField<>();
    private final ObservableField<Brief> mTo=new ObservableField<>();
    private final ObservableField<Progress> mProgress=new ObservableField<>();
    private Binding mBinding;
    private int mAutoDismiss;

    @Override
    protected View onCreateContent(Context context) {
        DoingTaskBinding binding=inflate(context,R.layout.doing_task);
        binding.setDoing(this);
        return binding.getRoot();
    }

    @Override
    public void onProgressChanged(Task task, Progress progress) {
        mProgress.set(progress);
        mProgress.notifyChange();
        Object object=null!=progress?progress.getData():null;
        DoingFiles doingFiles=null!=object&&object instanceof DoingFiles?((DoingFiles)object):null;
        mFrom.set(null!=doingFiles?doingFiles.getFrom():null);
        mTo.set(null!=doingFiles?doingFiles.getTo():null);
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        switch (status){
            case Executor.STATUS_FINISH:
                Result result=null!=task?task.getResult():null;
                result=null!=result?result:new Response<>(Code.CODE_UNKNOWN,"Unknown error.");
                int autoDismiss=mAutoDismiss;
                if (result instanceof Confirm){
                    setConfirm(((Confirm)result));
                    return;
                }else if (autoDismiss>0){
                    post(()->removeFromParent(),autoDismiss>10000?10000:autoDismiss);//Auto dismiss
                }
//                mResult.set(result);
//                setNotify(getString(result.isSucceed()?R.string.succeed:R.string.fail));
                break;
        }
    }

    public final DoingContent setDoingBinding(Binding binding) {
        this.mBinding = binding;
        return this;
    }

    public final DoingContent setAutoDismiss(int autoDismiss) {
        this.mAutoDismiss = autoDismiss;
        return this;
    }

    public final DoingContent setTitle(String name){
        mTitle.set(name);
        return this;
    }

    public ObservableField<Progress> getProgress(){
        return mProgress;
    }

    public ObservableField<String> getTitle(){
        return mTitle;
    }

    public ObservableField<Brief> getFrom() {
        return mFrom;
    }

    public ObservableField<Brief> getTo() {
        return mTo;
    }

    public Binding getDoingBinding(){
        return mBinding;
    }
}
