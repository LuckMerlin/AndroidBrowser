package com.luckmerlin.browser;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.databinding.ItemConveyorGroupBinding;
import com.luckmerlin.browser.databinding.ItemConveyorSingleBinding;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.ConfirmResult1;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskGroup;
import com.merlin.adapter.PageListAdapter;
import com.merlin.adapter.Query;

import java.util.ArrayList;
import java.util.List;

public class ConveyorListAdapter extends PageListAdapter<Query<Task>, Task> {
    private final static int VIEW_TYPE_DATA_GROUP=2000;
    private final ObservableField<Boolean> mMultiChooseEnabled=new ObservableField<>(false);
    private List<Task> mSelectedList;

    public ConveyorListAdapter(){
        setFixedHolder(VIEW_TYPE_EMPTY,R.layout.item_content_empty);
    }

    public boolean toggleMultiSelect(){
        Boolean enable=mMultiChooseEnabled.get();
        return enableMultiSelect(!(null!=enable&&enable));
    }

    public boolean enableMultiSelect(boolean enable){
        Boolean current=mMultiChooseEnabled.get();
        if (enable!=(null!=current&&current)){
            mMultiChooseEnabled.set(enable);
            notifyAttachedItemChanged();
            return true;
        }
        return true;
    }

    public boolean toggleSelect(Object obj){
        List<Task> select=mSelectedList;
        return null!=obj&&null!=select&&select.contains(obj)?unSelect(select):select(select);
    }

    public boolean unSelect(Object obj){
        List<Task> select=mSelectedList;
        return null!=obj&&obj instanceof Task&&null!=select&&select.remove(obj)&&
                notifyFirstItemChanged(obj,"UnSelected");
    }

    public boolean select(Object obj){
        if (null!=obj&&obj instanceof Task){
            List<Task> select=mSelectedList;
            select=null!=select?select:(mSelectedList=new ArrayList<>());
            if (!select.contains(obj)){
                select.add((Task) obj);
                return notifyFirstItemChanged(select,"SelectedChanged");
            }
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType=super.getItemViewType(position);
        if (viewType==VIEW_TYPE_DATA){
            Task task=getItem(position);
            viewType=null!=task&&task instanceof TaskGroup ?VIEW_TYPE_DATA_GROUP:viewType;
        }
        return viewType;
    }

    @Override
    protected Object onCreateViewTypeHolder(int viewType, ViewGroup parent) {
        if (viewType==VIEW_TYPE_DATA_GROUP){
            return null!=parent?inflateViewHolder(parent.getContext(),R.layout.item_conveyor_group):null;
        }else if (viewType==VIEW_TYPE_DATA){
            return null!=parent?inflateViewHolder(parent.getContext(),R.layout.item_conveyor_single):null;
        }
        return super.onCreateViewTypeHolder(viewType, parent);
    }

    public final ConveyorListAdapter addTaskWithSort(Task task){
        add(task);
        return this;
    }

    @Override
    protected ViewGroup.LayoutParams onCreateViewHolderLayoutParams(ViewGroup parent, int viewType, RecyclerView.ViewHolder viewHolder) {
        if (viewType==VIEW_TYPE_DATA||viewType==VIEW_TYPE_DATA_GROUP){
            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin=2;
            params.topMargin=2;
            return params;
        }
        return super.onCreateViewHolderLayoutParams(parent, viewType, viewHolder);
    }

    @Override
    protected void onBindData(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        super.onBindData(holder, position, payloads);
        View itemView=null!=holder?holder.itemView:null;
        ViewDataBinding binding=null!=itemView? DataBindingUtil.getBinding(itemView):null;
        if (null!=binding){
            Task item=getItem(position);
            Object result=null;
            Progress progress=null;
            if (null!=item){
                result=item.getResult();
                progress=item.getProgress();
            }
            int iconRes;
            Confirm confirm=null;
            if (null==result){
                iconRes=null!=progress?R.drawable.selector_pause:R.drawable.selector_start;
            }else if (result instanceof Confirm){
                iconRes=R.drawable.selector_confirm;
                confirm=((Confirm)result);
            }else{
                iconRes=null==progress||progress.intValue()!=100? R.drawable.selector_fail:R.drawable.selector_succeed;
            }
            List<Task> selectList=mSelectedList;
            Boolean selectEnabled=mMultiChooseEnabled.get();
            selectEnabled=null!=selectEnabled&&selectEnabled;
            boolean selected=selectEnabled&&null!=item&&null!=selectList&&selectList.contains(item);
            if (binding instanceof ItemConveyorGroupBinding){
                ItemConveyorGroupBinding groupBinding=(ItemConveyorGroupBinding)binding;
                groupBinding.setPosition(position);
                groupBinding.setConfirm(confirm);
                groupBinding.setSelected(selected);
                groupBinding.setSelectEnable(selectEnabled);
                groupBinding.setIconBinding(ViewBinding.clickId(iconRes,item));
                groupBinding.setTask(item instanceof TaskGroup?(TaskGroup)item:null);
            }else if (binding instanceof ItemConveyorSingleBinding){
                ItemConveyorSingleBinding singleBinding=(ItemConveyorSingleBinding)binding;
                singleBinding.setPosition(position);
                singleBinding.setSelected(selected);
                singleBinding.setSelectEnable(selectEnabled);
                singleBinding.setIconBinding(ViewBinding.clickId(iconRes,item));
                singleBinding.setConfirm(confirm);
                singleBinding.setTask(item);
            }
        }
    }

    public ObservableField<Boolean> getMultiChooseEnabled() {
        return mMultiChooseEnabled;
    }
}
