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
import com.luckmerlin.browser.task.FileDeleteTask;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Task;
import com.merlin.model.OnActivityCreate;
import com.merlin.model.OnActivityDestroy;
import com.merlin.model.OnBackPress;

import java.io.File;

public class ConveyorActivityModel extends BaseModel implements
        OnActivityCreate, OnActivityDestroy, OnBackPress, OnClickListener {
    private ServiceConnection mServiceConnection;
    private ConveyorListAdapter mConveyorListAdapter=new ConveyorListAdapter();
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
//        mExecutor=executor;
        if (null!=executor){
//            for (int i = 0; i < 100; i++) {
//                StreamCopyTask streamCopyTask=new StreamCopyTask
//                        (new AndroidFileStream(new File("/sdcard/test.png")),
//                                new AndroidFileStream(new File(
//                                        "/sdcard/test"+i+".png")),null);
//                mConveyorListAdapter.add(streamCopyTask);
//                executor.execute(streamCopyTask,null);
//            }
            FileDeleteTask deleteTask=new FileDeleteTask(LocalClient.createLoadFile
                (new File("/sdcard/test.png")),null);
        deleteTask.setName("删除文件 ");
            mConveyorListAdapter.add(deleteTask);
            executor.execute(deleteTask,null);
        }
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.drawable.selector_back:
                return onBackPressed();
            case R.drawable.selector_confirm:
                Result result=null!=obj&&obj instanceof Task?((Task)obj).getResult():null;
                ConfirmResult.Confirm confirm=null!=result&&result instanceof ConfirmResult?((ConfirmResult)result).make(getContext()):null;
                if (null==confirm){
                    return toast(R.string.error,0)||true;
                }
                return null!=showContentDialog(new ConfirmDialogContent(confirm),null);
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
