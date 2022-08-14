package com.luckmerlin.browser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import androidx.databinding.ViewDataBinding;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.databinding.ConveyorActivityBinding;
import com.luckmerlin.browser.dialog.ConfirmDialogContent;
import com.luckmerlin.browser.task.AndroidFileStream;
import com.luckmerlin.browser.task.FileCopyTask;
import com.luckmerlin.browser.task.FileDeleteTask;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskGroup;
import com.merlin.model.OnActivityCreate;
import com.merlin.model.OnActivityDestroy;
import com.merlin.model.OnBackPress;

import java.io.File;

public class ConveyorActivityModel extends BaseModel implements
        OnActivityCreate, OnActivityDestroy, OnBackPress, OnClickListener {
    private ServiceConnection mServiceConnection;
    private ConveyorListAdapter mConveyorListAdapter=new ConveyorListAdapter();
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
//        new FileCopyTask(LocalClient.createLoadFile(new File("/sdcard/test.png")),
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

    private void setTaskExecutor(Executor executor){
        mExecutor=executor;
        if (null!=executor){
//            for (int i = 0; i < 100; i++) {
//                StreamSourceCopyTask streamCopyTask=new StreamSourceCopyTask
//                        (new AndroidFileStream(new File("/sdcard/test.png")),
//                                new AndroidFileStream(new File(
//                                        "/sdcard/test"+i+".png")),null);
//                mConveyorListAdapter.add(streamCopyTask);
//                executor.execute(streamCopyTask,null);
//            }


            FileCopyTask copyTask=new FileCopyTask(LocalClient.createLocalFile
//                    (new File("/sdcard/Test")),LocalClient.createLocalFile
                    (new File("/sdcard/TestNew")),LocalClient.createLocalFile
                    (new File("/sdcard/Test2")),null);
            copyTask.setName("任务名字");
            mConveyorListAdapter.add(copyTask);
            executor.execute(copyTask,null);

            for (int i = 0; i < 1; i++) {
//                FileCopyTask copyTask=new FileCopyTask(LocalClient.createLocalFile(new File("/sdcard/test.png")),
//                        LocalClient.createLocalFile(new File("/sdcard/test"+i+".png")),null);
//                mConveyorListAdapter.add(copyTask);
//                executor.execute(copyTask,null);
            }
            //
//            TaskGroup group=new TaskGroup(null);
//            group.setName("删除群组");
//            for (int i = 0; i < 100; i++) {
//                FileDeleteTask deleteTask=new FileDeleteTask(LocalClient.createLocalFile
//                        (new File("/sdcard/test"+i+".png")),null);
//                group.add(deleteTask.enableConfirm(false));
//            }
//            mConveyorListAdapter.add(group);
//            executor.execute(group,null);
        }
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.drawable.selector_back:
                return onBackPressed();
            case R.drawable.selector_confirm:
                Task task=null!=obj&&obj instanceof Task?(Task)obj:null;
                Result result=null!=task?task.getResult():null;
                ConfirmResult.Confirm confirm=null!=result&&result instanceof ConfirmResult?((ConfirmResult)result).make(getContext()):null;
                if (null==confirm){
                    return toast(R.string.error,0)||true;
                }
                return null!=showContentDialog(new ConfirmDialogContent(confirm){
                    @Override
                    protected void onConfirmFinish(boolean confirmed, Object confirmObj) {
                        Executor executor=confirmed&&null!=confirmObj?mExecutor:null;
                        if (null!=executor){
                            executor.execute(task,null);
                        }
                    }
                },null);
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return finishActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, Activity activity) {
         Intent intent=new Intent(activity, ConveyorService.class);
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
         },Context.BIND_ABOVE_CLIENT|Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
    }

    @Override
    public void onDestroy(Activity activity) {
        ServiceConnection serviceConnection=mServiceConnection;
        if (null!=serviceConnection){
            mServiceConnection=null;
            unbindService(serviceConnection);
        }
    }

    public final ConveyorListAdapter getConveyorListAdapter() {
        return mConveyorListAdapter;
    }
}
