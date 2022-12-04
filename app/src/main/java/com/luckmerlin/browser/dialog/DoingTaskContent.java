package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import com.luckmerlin.binding.Binding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DoingTaskContentBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.Brief;
import com.luckmerlin.task.Doing;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.FromTo;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Ongoing;
import com.luckmerlin.task.Task;

public class DoingTaskContent extends ConfirmContent implements
        Executor.OnStatusChangeListener, OnProgressChange,OnClickListener{
    private final ObservableField<Doing> mDoing=new ObservableField<>();
    private final ObservableField<Binding> mBinding=new ObservableField<>();
    private final ObservableField<Integer> mProgress=new ObservableField<>();
    private final ObservableField<String> mDoingName=new ObservableField<>();
    private final ObservableField<Brief> mFromBrief=new ObservableField<>();
    private final ObservableField<Brief> mToBrief=new ObservableField<>();
    private final ObservableField<String> mSpeed=new ObservableField<>();
    private AutoDismiss mAutoDismiss;

    public interface AutoDismiss{
        int onResolveAutoDismiss(Result result);
    }

    @Override
    protected View onCreateContent(Context context) {
        DoingTaskContentBinding binding=inflate(context,R.layout.doing_task_content);
        binding.setContent(this);
        return binding.getRoot();
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        return removeFromParent()||true;
    }

    @Override
    public void onProgressChanged(Task task) {
        if (!isUiThread()){
            post(()->onProgressChanged(task));
            return;
        }
        Ongoing ongoing=null!=task?task.getOngoing():null;
        mDoing.set(null!=ongoing?ongoing.getDoing():null);
        mBinding.set(null!=ongoing?ongoing.getBinding():null);
        mProgress.set(null!=ongoing?ongoing.getProgress():0);
        mDoingName.set(null!=ongoing?ongoing.getTitle():null);
        mSpeed.set(null!=ongoing?ongoing.getSpeed():null);
        setConfirm(null!=ongoing?ongoing.getConfirm():null);
        FromTo fromTo=null!=ongoing?ongoing.getFromTo():null;
        Object from=null!=fromTo?fromTo.getFrom():null;
        mFromBrief.set(null!=from&&from instanceof Brief?(Brief) from:null);
        Object to=null!=fromTo?fromTo.getTo():null;
        mToBrief.set(null!=to&&to instanceof Brief?(Brief) to:null);
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        Ongoing ongoing=null!=task?task.getOngoing():null;
        mDoing.set(null!=ongoing?ongoing.getDoing():null);
        mBinding.set(null!=ongoing?ongoing.getBinding():null);
        mProgress.set(null!=ongoing?ongoing.getProgress():0);
        mDoingName.set(null!=ongoing?ongoing.getTitle():null);
        mSpeed.set(null!=ongoing?ongoing.getSpeed():null);
        setConfirm(null!=ongoing?ongoing.getConfirm():null);
        FromTo fromTo=null!=ongoing?ongoing.getFromTo():null;
        Object from=null!=fromTo?fromTo.getFrom():null;
        mFromBrief.set(null!=from&&from instanceof Brief?(Brief) from:null);
        Object to=null!=fromTo?fromTo.getTo():null;
        mToBrief.set(null!=to&&to instanceof Brief?(Brief) to:null);
    }

    public final DoingTaskContent setDoingBinding(Binding binding){
        if (!isUiThread()){
            post(()->setDoingBinding(binding));
            return this;
        }
        mBinding.set(binding);
        return this;
    }


    public final DoingTaskContent setAutoDismiss(AutoDismiss autoDismiss) {
        this.mAutoDismiss = autoDismiss;
        return this;
    }

    public ObservableField<Integer> getProgress() {
        return mProgress;
    }

    public ObservableField<String> getDoingName() {
        return mDoingName;
    }

    public final ObservableField<Doing> getDoing() {
        return mDoing;
    }

    public final ObservableField<Binding> getBinding(){
        return mBinding;
    }

    public ObservableField<String> getSpeed() {
        return mSpeed;
    }

    public ObservableField<Brief> getFromBrief() {
        return mFromBrief;
    }

    public ObservableField<Brief> getToBrief() {
        return mToBrief;
    }
}
