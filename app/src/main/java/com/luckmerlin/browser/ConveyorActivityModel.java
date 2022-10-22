package com.luckmerlin.browser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;

import com.luckmerlin.binding.BindingGroup;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.ConveyorActivityBinding;
import com.luckmerlin.browser.dialog.ConfirmContent;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.browser.dialog.ModelMenuItemModel;
import com.luckmerlin.browser.dialog.TaskMenuContextDialogContent;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.model.OnBackPress;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.RestartEnabler;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Task;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;

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
            final ModelMenuItemModel menuItem=new ModelMenuItemModel(R.drawable.selector_menu);
            setRightMenuBinding(new BindingGroup(menuItem));
            final Observable.OnPropertyChangedCallback changedCallback=new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    ObservableField<Boolean> field=mConveyorListAdapter.getMultiChooseEnabled();
                    Boolean multiChoose=null!=field?field.get():null;
                    menuItem.setMenuBinding(ViewBinding.clickId(multiChoose!=null&&multiChoose?
                            R.drawable.selector_cancel: R.drawable.selector_menu));
                }
            };
            mConveyorListAdapter.getMultiChooseEnabled().addOnPropertyChangedCallback(changedCallback);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        mConveyorListAdapter.replaceTask(task,status==Executor.STATUS_REMOVE);
    }

    private void setTaskExecutor(Executor executor){
        Executor current=mExecutor;
        if (null!=current){
            current.removeListener(this);
        }
        mExecutor=executor;
        if (null!=executor){
            executor.putListener(this,null,true);
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
                return showTaskDialog(mExecutor,task,null);
            case R.layout.item_conveyor_single:
            case R.layout.item_conveyor_group:
                return deleteTask(null!=obj&&obj instanceof Task?(Task)obj:null)||true;
            case R.drawable.selector_succeed:
                return ((null!=obj&&obj instanceof Task&&restartTask((Task)obj,true)))||true;
        }
        return false;
    }

    private boolean restartTask(Task task,boolean enable){
        return null!=task&&task instanceof RestartEnabler&&((RestartEnabler)task).enableRestart(enable);
    }

    private boolean deleteTask(Task task){
        if (null==task){
            return false;
        }
        final ConfirmContent confirmContent=new ConfirmContent();
        String title=getString(R.string.sureWhich,getString(R.string.delete));
        Confirm confirm=new Confirm();
        confirm.setName(title).setMessage(task.getName());
        confirm.setBinding(new DialogButtonBinding(ViewBinding.clickId(R.string.sure).
                setListener((OnClickListener) (View view1, int clickId1, int count1, Object obj1)->
                        ((confirmContent.removeFromParent()||true)&&executeTask(task,
                                Option.DELETE|Option.CANCEL)&&
                                mConveyorListAdapter.remove(task))||true),
                ViewBinding.clickId(R.string.cancel).setListener((OnClickListener) (View view1, int clickId1, int count1, Object obj1)->
                        confirmContent.removeFromParent()||true)));
        confirmContent.setConfirm(confirm);
        return null!=showContentDialog(confirmContent, new FixedLayoutParams().wrapContentAndCenter());
    }

    private boolean showMenusDialog(){
        return null!=showContentDialog(new TaskMenuContextDialogContent().setTitle
                (getString(R.string.app_name)), getContext(),new FixedLayoutParams().dialog());
    }

    @Override
    public boolean onBackPressed() {
        return mConveyorListAdapter.enableMultiSelect(false)||finishActivity();
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

    private boolean executeTask(Object task,int option){
        Executor executor=null!=task?mExecutor:null;
        return null!=executor&&executor.execute(task, option);
    }

    public final ConveyorListAdapter getConveyorListAdapter() {
        return mConveyorListAdapter;
    }
}
