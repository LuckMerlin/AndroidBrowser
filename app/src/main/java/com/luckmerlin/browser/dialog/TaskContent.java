//package com.luckmerlin.browser.dialog;
//
//import android.content.Context;
//import android.view.View;
//import androidx.databinding.ObservableField;
//
//import com.luckmerlin.browser.databinding.TaskContentDialogBinding;
//import com.luckmerlin.core.Code;
//import com.luckmerlin.browser.R;
//import com.luckmerlin.click.OnClickListener;
//import com.luckmerlin.core.Response;
//import com.luckmerlin.core.Result;
//import com.luckmerlin.task.Confirm1;
//import com.luckmerlin.task.Executor;
//import com.luckmerlin.task.OnProgressChange;
//import com.luckmerlin.task.Task;
//
//@Deprecated
//public class TaskContent extends ConfirmContent implements OnClickListener,
//        Executor.OnStatusChangeListener, OnProgressChange {
//    private ObservableField<String> mNotify=new ObservableField<>();
//    private int mAutoDismiss;
//    private final ObservableField<DoingFiles> mDoingFiles=new ObservableField<>();
//    private final ObservableField<Result> mResult=new ObservableField<>();
//    private final ObservableField<Integer> mTaskProgress=new ObservableField<>();
//
//    @Override
//    protected View onCreateContent(Context context) {
//        TaskContentDialogBinding binding=inflate(context, R.layout.task_content_dialog);
//        if (null!=binding){
//            binding.setContent(this);
//            return binding.getRoot();
//        }
//        return null;
//    }
//
//    @Override
//    public boolean onClick(View view, int clickId, int count, Object obj) {
//        switch (clickId){
//            case R.drawable.selector_close:
//            case R.string.close:
//            case R.string.succeed:
//            case R.string.cancel:
//                return removeFromParent()||true;
//        }
//        return false;
//    }
//
//    @Override
//    public void onProgressChanged(Task task) {
////        if (null!=progress){
////            mTaskProgress.set(progress.intValue());
////            Object object=progress.getData();
////            if (null!=object&&object instanceof DoingFiles){
////                mDoingFiles.set((DoingFiles) object);
////                mDoingFiles.notifyChange();
////            }
////        }
//    }
//
//    @Override
//    public void onStatusChanged(int status, Task task, Executor executor) {
//        setConfirm(null);//Clean
//        switch (status){
//            case Executor.STATUS_FINISH:
////                Result result=null!=task?task.getResult():null;
//                Result result=null;
//                result=null!=result?result:new Response<>(Code.CODE_UNKNOWN,"Unknown error.");
//                int autoDismiss=mAutoDismiss;
//                if (result instanceof Confirm1){
////                    setConfirm((Confirm1)result);
//                    return;
//                }else if (autoDismiss>0){
//                    post(()->removeFromParent(),autoDismiss>10000?10000:autoDismiss);//Auto dismiss
//                }
//                mResult.set(result);
////                setNotify(getString(result.isSucceed()?R.string.succeed:R.string.fail));
//                break;
//        }
//    }
//
//    public final TaskContent setNotify(String notify){
//        if (!isUiThread()){
//            post(()->setNotify(notify));
//            return this;
//        }
//        mNotify.set(notify);
//        return this;
//    }
//
//    public TaskContent setAutoDismiss(int autoDismiss) {
//        this.mAutoDismiss = autoDismiss;
//        return this;
//    }
//
//    public final ObservableField<String> getNotify() {
//        return mNotify;
//    }
//
//    public ObservableField<DoingFiles> getDoingFiles() {
//        return mDoingFiles;
//    }
//
//    public ObservableField<Result> getResult() {
//        return mResult;
//    }
//
//    public ObservableField<Integer> getTaskProgress() {
//        return mTaskProgress;
//    }
//}
