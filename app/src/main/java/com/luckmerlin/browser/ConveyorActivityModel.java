package com.luckmerlin.browser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.ConveyorActivityBinding;
import com.luckmerlin.browser.dialog.ConfirmDialogContent;
import com.luckmerlin.browser.dialog.TaskMenuContextDialogContent;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.task.ConfirmResult1;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskExecutor;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;
import com.merlin.model.OnBackPress;

public class ConveyorActivityModel extends BaseModel implements
        OnViewAttachedToWindow, OnViewDetachedFromWindow, OnBackPress, OnClickListener,
        Executor.OnStatusChangeListener {
    private ServiceConnection mServiceConnection;
    private final ConveyorListAdapter mConveyorListAdapter=new ConveyorListAdapter();
    private Executor mExecutor;

    @Override
    protected View onCreateContent(Context context) {
        //
//        new File("/sdcard/test2.png").delete();
//        streamCopyTask.setName("测司法所大发送");
//        streamCopyTask.setConvertor(new CoderConvertor());
        //
//        FileDeleteTask deleteTask=new FileDeleteTask(LocalClient.createLoadFile
//                (new File("/sdcard/test.png")),null);
//        deleteTask.setName("删除文件 ");
//        mConveyorListAdapter.add(deleteTask);
//        deleteTask.execute(null);
//        //
//        new FileCopyTask1(LocalClient.createLoadFile(new File("/sdcard/test.png")),
//                LocalClient.createLoadFile(new File("/sdcard/test2.png")),null);
//        //
//        new Thread(()->streamCopyTask.execute(null)).start();

//        try {
//            byte[] src="我和我的祖国一刻也不能分离".getBytes("utf-8");
//            byte[] test = "我们的".getBytes("utf-8");
//            byte[] target=new byte[src.length];
//            //
//            for (int i = 0; i < src.length; i++) {
//                int sec=i%test.length;
//                target[i]=src[i];
//                if (sec%2==0){
//                    target[i]=(byte) (~src[i]);
//                    target[i]=(byte)(~target[i]);
//                }
//            }
//            Debug.D("jieguo="+new String(target,"utf-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        //
//        for (int i = 0; i < 100; i++) {
//            AbstractTask task=null;
//            if (i%8<4){
//                mConveyorListAdapter.add((task=new TestTask((Activity) context)).setName("单 "+i));
//            }else{
//                mConveyorListAdapter.add(task=new TaskGroup(null).setName("多 "+i));
//            }
//            final Task finalTask=task;
//            new Thread(()->{
//                finalTask.execute(null,null);
//            }).start();
//        }
        //
        ViewDataBinding binding= DataBindingUtil.inflate(context,R.layout.conveyor_activity);
        if (null!= binding&&binding instanceof ConveyorActivityBinding){
            ((ConveyorActivityBinding)binding).setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        switch (status){
            case Executor.STATUS_ADD:
                mConveyorListAdapter.addTaskWithSort(task);
                break;
            case Executor.STATUS_DELETE:
                mConveyorListAdapter.remove(task);
                break;
        }
    }

    private void setTaskExecutor(Executor executor){
        Executor current=mExecutor;
        if (null!=current){
            current.removeListener(this);
        }
        mExecutor=executor;
        if (null!=executor){
            executor.putListener(this,null,false);
            executor.match((TaskExecutor.ExecuteTask data)-> null!=mConveyorListAdapter.
                    addTaskWithSort(null!=data?data.getTask():null));
            for (int i = 0; i < 100; i++) {
//                StreamSourceCopyTask streamCopyTask=new StreamSourceCopyTask
//                        (new AndroidFileStream(new File("/sdcard/test.png")),
//                                new AndroidFileStream(new File(
//                                        "/sdcard/test"+i+".png")),null);
//                mConveyorListAdapter.add(streamCopyTask);
//                executor.execute(streamCopyTask,null);
            }


//            FileCopyTask1 copyTask=new FileCopyTask1(LocalClient.createLocalFile
////                    (new File("/sdcard/Test")),LocalClient.createLocalFile
//                    (new File("/sdcard/TestNew")),LocalClient.createLocalFile
//                    (new File("/sdcard/Test2")),null);
//            copyTask.setName("任务名字");
//        FileCopyTask1 copyTask=null;
//        copyTask=new FileCopyTask1(LocalClient.createLocalFile
//                    (new File("/sdcard/Test")),LocalClient.createLocalFile
        }
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.drawable.selector_back:
                return onBackPressed();
            case R.string.multiChoose:
                return mConveyorListAdapter.enableMultiSelect(true)||true;
            case R.drawable.selector_cancel:
                return mConveyorListAdapter.enableMultiSelect(false)||true;
            case R.drawable.selector_menu:
                return showMenusDialog()||true;
            case R.drawable.selector_checkbox:
                return mConveyorListAdapter.toggleSelect(obj)||true;
            case R.drawable.selector_confirm:
                Task task=null!=obj&&obj instanceof Task?(Task)obj:null;
                Result result=null!=task?task.getResult():null;
                ConfirmResult1.Confirm confirm=null!=result&&result instanceof ConfirmResult1 ?
                        ((ConfirmResult1)result).make(getContext()):null;
                return null!=showContentDialog(new ConfirmDialogContent(confirm).setOnConfirmFinish(
                        (boolean confirmed, Object confirmObj)-> executeTask(confirmObj)),null);
            case R.layout.item_conveyor_single:
            case R.layout.item_conveyor_group:
                return null!=showContentDialog(new ConfirmDialogContent(new ConfirmResult1.
                        Confirm().setOnConfirm((boolean confirmed)-> confirmed?null:null).
                        setTitle(getString(R.string.delete)).setMessage(getString
                        (R.string.areYourSureWhich,getText(R.string.delete)))).setOnConfirmFinish((boolean confirmed, Object confirmObj)-> {
                            if (cancelTask(obj, Executor.Option.DELETE)&&null!=obj&&obj instanceof Task) {
                                mConveyorListAdapter.remove((Task) obj);
                            }
                            return null;
                        }),null);
        }
        return false;
    }

    private boolean showMenusDialog(){
        return null!=showContentDialog(new TaskMenuContextDialogContent().setTitle
                (getString(R.string.app_name)), getContext(),new FixedLayoutParams().dialog());
    }

    @Override
    public boolean onBackPressed() {
        return finishActivity();
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        Intent intent=new Intent(v.getContext(), ConveyorService.class);
        startService(intent);
        bindService(intent, mServiceConnection=new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Debug.D("EEEE onServiceConnected "+service);
                        if (null!=service&&service instanceof Executor){
                            setTaskExecutor((Executor)service);
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Debug.D("EEEE onServiceDisconnected "+name);
                    }
                },
                Context.BIND_ABOVE_CLIENT|Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        ServiceConnection serviceConnection=mServiceConnection;
        if (null!=serviceConnection){
            mServiceConnection=null;
            unbindService(serviceConnection);
        }
    }

    private boolean cancelTask(Object task, int option){
        Executor executor=null!=task?mExecutor:null;
        return null!=executor&&executor.execute(task, option|Option.CANCEL);
    }

    private boolean executeTask(Object task){
        Executor executor=null!=task?mExecutor:null;
        return null!=executor&&executor.execute(task, Option.NONE);
    }

    public final ConveyorListAdapter getConveyorListAdapter() {
        return mConveyorListAdapter;
    }
}
