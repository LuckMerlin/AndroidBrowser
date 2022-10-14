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
import com.luckmerlin.browser.task.FilesDeleteTask;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.object.ObjectCreator;
import com.luckmerlin.task.ConfirmResult1;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnTaskFind;
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
            executor.findTask((Task task, int status, int option)->
                    null!=task&&null!=mConveyorListAdapter.addTaskWithSort(task));
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
                            if (cancelTask(obj, Option.DELETE)&&null!=obj&&obj instanceof Task) {
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
        return null!=executor&&executor.execute(task, Option.EXECUTE);
    }

    public final ConveyorListAdapter getConveyorListAdapter() {
        return mConveyorListAdapter;
    }
}
