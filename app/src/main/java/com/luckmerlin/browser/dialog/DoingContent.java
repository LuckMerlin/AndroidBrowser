package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.core.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DoingTaskBinding;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.browser.CodeResult;
import com.luckmerlin.core.MessageResult;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.BindingResult;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskRestartEnabler;

public class DoingContent extends ConfirmContent implements
        Executor.OnStatusChangeListener, OnProgressChange,OnClickListener{
    private final ObservableField<String> mTitle=new ObservableField<>();
    private final ObservableField<CharSequence> mMessage=new ObservableField<>();
    private final ObservableField<Doing> mDoing=new ObservableField<>();
    private final ObservableField<Binding> mBinding=new ObservableField<>();
    private AutoDismiss mAutoDismiss;

    public interface AutoDismiss{
        int onResolveAutoDismiss(Result result);
    }

    @Override
    protected View onCreateContent(Context context) {
        DoingTaskBinding binding=inflate(context,R.layout.doing_task);
        binding.setDoing(this);
        return binding.getRoot();
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        return removeFromParent()||true;
    }

    @Override
    public void onProgressChanged(Task task, Progress progress) {
        if (!isUiThread()){
            post(()->onProgressChanged(task,progress));
            return;
        }
        Object object=null!=progress?progress.getDoing():null;
        mDoing.set(null!=object&&object instanceof Doing ?((Doing)object):null);
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        switch (status){
            case Executor.STATUS_EXECUTING:
                setConfirm(null);
                setMessage(null);
                setDoingBinding(new DialogButtonBinding(ViewBinding.clickId(R.string.cancel).setListener(
                        (OnClickListener)(View view, int clickId, int count, Object obj)->
                                null!=executor&&executor.execute(task,Option.CANCEL))));
                break;
            case Executor.STATUS_FINISH:
                Result result=null!=task?task.getResult():null;
                result=result instanceof ConfirmResult?((ConfirmResult)result).makeConfirm(getContext()):result;
                result=null!=result?result:new Response<>(Code.CODE_UNKNOWN,"Unknown error.");
                Binding binding=null;
                if (result instanceof BindingResult){
                    binding=((BindingResult)result).getBinding();
                }
                if (result instanceof Confirm){
                    setConfirm(((Confirm)result));
                    return;
                }
                AutoDismiss autoDismiss=mAutoDismiss;
                int autoDismissDelay=null!=autoDismiss?autoDismiss.onResolveAutoDismiss(result):-1;
                if (autoDismissDelay>=0){
                    post(()->removeFromParent(),autoDismissDelay>10000?10000:autoDismissDelay);//Auto dismiss
                }
                setMessage(result instanceof MessageResult?((MessageResult)result).getMessage():null);
                if (null==binding){
                    int textResId=R.string.succeed;
                    if (!result.isSucceed()){
                        textResId=R.string.fail;
                        if (result instanceof CodeResult&&((CodeResult)result).getCode(Code.CODE_UNKNOWN)==Code.CODE_CANCEL){
                            textResId=R.string.cancel;
                        }
                    }
                    DialogButtonBinding buttonBinding=new DialogButtonBinding(ViewBinding.clickId(textResId).setListener
                            ((OnClickListener) (View view, int clickId, int count, Object obj)-> removeFromParent()||true));
                    //
                    if (null!=task&&task instanceof TaskRestartEnabler&&((TaskRestartEnabler)task).isTaskRestartEnable()){
                        buttonBinding.add(ViewBinding.clickId(R.string.restart).setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
                                (null!=executor&&executor.execute(task, Option.EXECUTE))||true));
                    }
                    //
                    buttonBinding.add(ViewBinding.clickId(R.string.remove).setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
                            (null!=executor&&executor.execute(task, Option.DELETE)&& removeFromParent())||true));
                    binding=buttonBinding;
                }
                setDoingBinding(binding);
                break;
        }
    }

    public final DoingContent setDoingBinding(Binding binding){
        if (!isUiThread()){
            post(()->setDoingBinding(binding));
            return this;
        }
        mBinding.set(binding);
        return this;
    }

    public final DoingContent setMessage(CharSequence message){
        if (!isUiThread()){
            post(()->setMessage(message));
            return this;
        }
        mMessage.set(message);
        return this;
    }

    public final DoingContent setAutoDismiss(AutoDismiss autoDismiss) {
        this.mAutoDismiss = autoDismiss;
        return this;
    }

    public final DoingContent setTitle(String name){
        mTitle.set(name);
        return this;
    }

    public final ObservableField<String> getTitle(){
        return mTitle;
    }

    public final ObservableField<Doing> getDoing() {
        return mDoing;
    }

    public final ObservableField<Binding> getDoingBinding(){
        return mBinding;
    }

    public final ObservableField<CharSequence> getMessage() {
        return mMessage;
    }
}
