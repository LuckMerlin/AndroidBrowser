package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import com.luckmerlin.binding.Binding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DoingTaskContentBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
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
        setConfirm(null!=ongoing?ongoing.getConfirm():null);
        FromTo fromTo=null!=ongoing?ongoing.getFromTo():null;
        Object from=null!=fromTo?fromTo.getFrom():null;
        mFromBrief.set(null!=from&&from instanceof Brief?(Brief) from:null);
        Object to=null!=fromTo?fromTo.getTo():null;
        mToBrief.set(null!=to&&to instanceof Brief?(Brief) to:null);
        Debug.D("AAAAA "+status+" "+executor);
        switch (status){
            case Executor.STATUS_FINISH:
        }
//        switch (status){
//            case Executor.STATUS_EXECUTING:
//                setConfirm(null);
//                setMessage(null);
//                setDoingBinding(new DialogButtonBinding(ViewBinding.clickId(R.string.cancel).setListener(
//                        (OnClickListener)(View view, int clickId, int count, Object obj)->
//                                null!=executor&&executor.execute(task,Option.CANCEL))));
//                break;
//            case Executor.STATUS_FINISH:
//                Result result=null!=task?task.getResult():null;
//                result=result instanceof ConfirmResult?((ConfirmResult)result).makeConfirm(getContext()):result;
//                result=null!=result?result:new Response<>(Code.CODE_UNKNOWN,"Unknown error.");
//                Binding binding=null;
//                if (result instanceof BindingResult){
//                    binding=((BindingResult)result).getBinding();
//                }
//                if (result instanceof Confirm1){
//                    setConfirm(((Confirm1)result));
//                    return;
//                }
//                AutoDismiss autoDismiss=mAutoDismiss;
//                int autoDismissDelay=null!=autoDismiss?autoDismiss.onResolveAutoDismiss(result):-1;
//                if (autoDismissDelay>=0){
//                    post(()->removeFromParent(),autoDismissDelay>10000?10000:autoDismissDelay);//Auto dismiss
//                }
//                setMessage(result instanceof MessageResult?((MessageResult)result).getMessage():null);
//                if (null==binding){
//                    int textResId=R.string.succeed;
//                    if (!result.isSucceed()){
//                        textResId=R.string.fail;
//                        if (result instanceof CodeResult&&((CodeResult)result).getCode(Code.CODE_UNKNOWN)==Code.CODE_CANCEL){
//                            textResId=R.string.cancel;
//                        }
//                    }
//                    DialogButtonBinding buttonBinding=new DialogButtonBinding(ViewBinding.clickId(textResId).setListener
//                            ((OnClickListener) (View view, int clickId, int count, Object obj)-> removeFromParent()||true));
//                    //
//                    if (null!=task&&task instanceof TaskRestartEnabler&&((TaskRestartEnabler)task).isTaskRestartEnable()){
//                        buttonBinding.add(ViewBinding.clickId(R.string.restart).setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
//                                (null!=executor&&executor.execute(task, Option.EXECUTE))||true));
//                    }
//                    //
//                    buttonBinding.add(ViewBinding.clickId(R.string.remove).setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
//                            (null!=executor&&executor.execute(task, Option.DELETE)&& removeFromParent())||true));
//                    binding=buttonBinding;
//                }
//                setDoingBinding(binding);
//                break;
//        }
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

    public ObservableField<Brief> getFromBrief() {
        return mFromBrief;
    }

    public ObservableField<Brief> getToBrief() {
        return mToBrief;
    }
}
