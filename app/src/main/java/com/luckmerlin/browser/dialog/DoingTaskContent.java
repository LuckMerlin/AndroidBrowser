package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DoingTaskContentBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Brief;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.FromTo;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Ongoing;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Task;

public class DoingTaskContent extends ConfirmContent implements
        Executor.OnStatusChangeListener, OnProgressChange,OnClickListener{
    private final ObservableField<Integer> mProgress=new ObservableField<>();
    private final ObservableField<Integer> mSecondProgress=new ObservableField<>();
    private final ObservableField<String> mDoingName=new ObservableField<>();
    private final ObservableField<Brief> mFromBrief=new ObservableField<>();
    private final ObservableField<Brief> mToBrief=new ObservableField<>();
    private final ObservableField<String> mSpeed=new ObservableField<>();
    private AutoDismiss mAutoDismiss;
    private final Runnable mAutoDismissRunnable=()->removeFromParent();

    public interface AutoDismiss{
        int onResolveAutoDismiss(Task task);
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

    private void updateTask(Task task){
        if (!isUiThread()){
            post(()->updateTask(task));
            return;
        }
        Ongoing ongoing=null!=task?task.getOngoing():null;
        setConfirmBinding(null!=ongoing?ongoing.getBinding():null);
        mProgress.set(null!=ongoing?ongoing.getProgress():0);
        mSecondProgress.set(null!=ongoing?ongoing.getSecondProgress():0);
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
    public void onProgressChanged(Task task) {
        updateTask(task);
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        updateTask(task);
        switch (status){
            case Executor.STATUS_FINISH:
                Integer progress=mProgress.get();
                Integer secondProgress=mSecondProgress.get();
                boolean succeed=false;
                if (Ongoing.isProgressSucceed(progress)&&Ongoing.isProgressSucceed(secondProgress)){
                    succeed=true;
                    AutoDismiss autoDismiss=mAutoDismiss;
                    int delay=null!=autoDismiss?autoDismiss.onResolveAutoDismiss(task):-1;
                    if (delay>=0){
                        post(mAutoDismissRunnable,delay);
                    }
                }
                if (!isExistBinding()){
                    setConfirmBinding(new DialogButtonBinding(succeed?ViewBinding.clickId(R.string.succeed).
                            setListener((OnClickListener)(View view, int clickId, int count, Object obj)->
                                    removeFromParent()||true):
                            ViewBinding.clickId(R.string.restart).setListener((OnClickListener)
                                    (View view, int clickId, int count, Object obj)->
                                  (null!=executor&&executor.execute(task, Option.LAUNCH))||true),
                            ViewBinding.clickId(R.string.delete).setListener((OnClickListener)
                                    (View view, int clickId, int count, Object obj)->
                                   (null!=executor&&executor.execute(task, Option.DELETE)&&removeFromParent())||true),
                            ViewBinding.clickId(R.string.fail).setListener((OnClickListener)
                            (View view, int clickId, int count, Object obj)->
                                    removeFromParent()||true)));
                }
                break;
        }
    }

    public final DoingTaskContent setAutoDismiss(AutoDismiss autoDismiss) {
        this.mAutoDismiss = autoDismiss;
        return this;
    }

    private boolean isExistBinding(){
        androidx.databinding.ObservableField<Binding> field=getBinding();
        if (null!=field&&field.get()!=null){
            return true;
        }
        androidx.databinding.ObservableField<Confirm> confirmObservableField=getConfirm();
        return null!=confirmObservableField&&confirmObservableField.get()!=null;
    }

    public ObservableField<Integer> getProgress() {
        return mProgress;
    }

    public ObservableField<String> getDoingName() {
        return mDoingName;
    }

    public ObservableField<String> getSpeed() {
        return mSpeed;
    }

    public ObservableField<Brief> getFromBrief() {
        return mFromBrief;
    }

    public ObservableField<Integer> getSecondProgress() {
        return mSecondProgress;
    }

    public ObservableField<Brief> getToBrief() {
        return mToBrief;
    }
}
