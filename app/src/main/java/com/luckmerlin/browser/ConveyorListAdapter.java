package com.luckmerlin.browser;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.databinding.ItemConveyorGroupBinding;
import com.luckmerlin.browser.databinding.ItemConveyorSingleBinding;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskGroup;
import com.merlin.adapter.PageListAdapter;
import com.merlin.adapter.Query;
import java.util.List;

public class ConveyorListAdapter extends PageListAdapter<Query<Task>, Task> {
    private final static int VIEW_TYPE_DATA_GROUP=2000;

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
            Context context=itemView.getContext();
            Task item=getItem(position);
            Object result=null;
            Progress progress=null;
            if (null!=item){
                result=item.getResult();
                progress=item.getProgress();
            }
            int iconRes;Object iconResObj=null;
            ConfirmResult.Confirm confirm=null;
            if (null==result){
                iconRes=item.isPending()?R.drawable.selector_wait:null!=progress?R.drawable.selector_pause:R.drawable.selector_start;
            }else if (result instanceof ConfirmResult){
                iconResObj=result;
                iconRes=R.drawable.selector_confirm;
                confirm=((ConfirmResult)result).create(context);
            }else{
                iconRes=null==progress||progress.intValue()!=100? R.drawable.selector_fail:R.drawable.selector_succeed;
            }
            if (binding instanceof ItemConveyorGroupBinding){
                ItemConveyorGroupBinding groupBinding=(ItemConveyorGroupBinding)binding;
                groupBinding.setPosition(position);
                groupBinding.setIconBinding(ViewBinding.clickId(iconRes,iconResObj));
                groupBinding.setTask(item instanceof TaskGroup?(TaskGroup)item:null);
            }else if (binding instanceof ItemConveyorSingleBinding){
                ItemConveyorSingleBinding singleBinding=(ItemConveyorSingleBinding)binding;
                singleBinding.setPosition(position);
                singleBinding.setIconResId(iconRes);
                singleBinding.setTask(item);
            }
        }
    }
}
