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
import com.luckmerlin.browser.databinding.ConveyorActivityBinding;
import com.luckmerlin.browser.task.StreamCopyTask;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.AndroidFileStream;
import com.luckmerlin.stream.Convertor;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.Stream;
import com.luckmerlin.task.TaskExecutor;
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
        StreamCopyTask streamCopyTask=new StreamCopyTask
                (new AndroidFileStream(new File("/sdcard/testn.png")),
                        new AndroidFileStream(new File(
                                "/sdcard/test2.png")),null);
        new File("/sdcard/test2.png").delete();
        streamCopyTask.setName("测司法所大发送");
        streamCopyTask.setConvertor(new CoderConvertor());
        mConveyorListAdapter.add(streamCopyTask);
        new Thread(()->streamCopyTask.execute(null,null)).start();
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

    private void setTaskExecutor(TaskExecutor executor){
//        mExecutor=executor;
        if (null!=executor){

        }
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.drawable.selector_back:
                return onBackPressed();
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
                 if (null!=service&&service instanceof TaskExecutor){
                    setTaskExecutor((TaskExecutor)service);
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
