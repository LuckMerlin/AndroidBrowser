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
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskExecutor;
import com.luckmerlin.task.TaskGroup;
import com.merlin.model.OnActivityCreate;
import com.merlin.model.OnActivityDestroy;

public class ConveyorActivityModel extends BaseModel implements OnActivityCreate, OnActivityDestroy {
    private ServiceConnection mServiceConnection;
    private ConveyorListAdapter mConveyorListAdapter=new ConveyorListAdapter();
    private TaskExecutor mExecutor;

    @Override
    protected View onCreateContent(Context context) {
        //
        for (int i = 0; i < 1000; i++) {
            if (i%8<4){
                mConveyorListAdapter.add(new TestTask().setName("单 "+i));
            }else{
                mConveyorListAdapter.add(new TaskGroup().setName("多 "+i));
            }
        }
        //
        ViewDataBinding binding= DataBindingUtil.inflate(context,R.layout.conveyor_activity);
        if (null!= binding&&binding instanceof ConveyorActivityBinding){
            ((ConveyorActivityBinding)binding).setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    private void setTaskExecutor(TaskExecutor executor){
        mExecutor=executor;
        if (null!=executor){

        }
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
