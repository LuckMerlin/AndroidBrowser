package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import androidx.databinding.ObservableField;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.Bindings;
import com.luckmerlin.binding.LayoutBinding;
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
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;

public class DoingContent extends BaseContent implements OnClickListener,
        Executor.OnStatusChangeListener, OnProgressChange {
    private final ObservableField<String> mName=new ObservableField<>();
    private final ObservableField<Brief> mFrom=new ObservableField<>();
    private final ObservableField<Brief> mTo=new ObservableField<>();
    private final ObservableField<ConfirmResult.Confirm> mConfirm=new ObservableField<>();
    private final ObservableField<Progress> mProgress=new ObservableField<>();
    private Binding mBinding;
    private int mAutoDismiss;

    public DoingContent(){
        mName.set("沙发大厦发放单独发搭搭撒撒代发大萨达发发发大");
        mFrom.set(new Brief() {
            @Override
            public CharSequence getName() {
                return "沙发大厦发放单独发搭搭撒撒代发大萨达发发发大";
            }

            @Override
            public CharSequence getNote() {
                return "沙发大厦发放单独发搭搭撒撒代发大萨达发发发大";
            }

            @Override
            public Object getIcon() {
                return R.drawable.hidisk_icon_aac;
            }
        });
        mTo.set(mFrom.get());
    }

    @Override
    protected View onCreateContent(Context context) {
        mBinding=new DialogButtonBinding(ViewBinding.clickId(R.string.sure),
                ViewBinding.clickId(R.string.cancel));
        DoingTaskBinding binding=inflate(context,R.layout.doing_task);
        binding.setDoing(this);
//        Handler handler=new Handler();
//        boolean[] aaa=new boolean[1];
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mConfirm.set(aaa[0]?new ConfirmResult.Confirm().setMessage("eeee").setTitle("dddd"):null);
//                aaa[0]=!aaa[0];
//                handler.postDelayed(this,1000);
//            }
//        }, 3000);
        return binding.getRoot();
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.string.sure: return removeFromParent()||true;
            case R.string.cancel: return removeFromParent()||true;
        }
        return false;
    }

    @Override
    public void onProgressChanged(Task task, Progress progress) {
        mProgress.set(progress);
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
                if (result instanceof ConfirmResult){
                    setConfirm(((ConfirmResult)result).make(getContext()));
                    return;
                }else if (autoDismiss>0){
                    post(()->removeFromParent(),autoDismiss>10000?10000:autoDismiss);//Auto dismiss
                }
//                mResult.set(result);
//                setNotify(getString(result.isSucceed()?R.string.succeed:R.string.fail));
                break;
        }
    }

    public final DoingContent setConfirm(ConfirmResult.Confirm confirm){
        mConfirm.set(confirm);
        return this;
    }

    public final DoingContent setAutoDismiss(int autoDismiss) {
        this.mAutoDismiss = autoDismiss;
        return this;
    }

    public final DoingContent setName(String name){
        mName.set(name);
        return this;
    }

    public ObservableField<Progress> getProgress(){
        return mProgress;
    }

    public ObservableField<ConfirmResult.Confirm> getConfirm() {
        return mConfirm;
    }

    public ObservableField<String> getName(){
        return mName;
    }

    public ObservableField<Brief> getFrom() {
        return mFrom;
    }

    public ObservableField<Brief> getTo() {
        return mTo;
    }

    public Binding getClickBinding(){
        return mBinding;
    }
}
