package com.luckmerlin.browser;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import com.luckmerlin.browser.databinding.ItemConveyorGroupBinding;
import com.luckmerlin.browser.databinding.ItemConveyorSingleBinding;
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
            viewType=null!=task&&task instanceof TaskGroup?VIEW_TYPE_DATA_GROUP:viewType;
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
            Task item=getItem(position);
            if (binding instanceof ItemConveyorGroupBinding){
                ItemConveyorGroupBinding groupBinding=(ItemConveyorGroupBinding)binding;
                groupBinding.setPosition(position);
                groupBinding.setTask(item instanceof TaskGroup?(TaskGroup)item:null);
            }else if (binding instanceof ItemConveyorSingleBinding){
                ItemConveyorSingleBinding singleBinding=(ItemConveyorSingleBinding)binding;
                singleBinding.setPosition(position);
                singleBinding.setTask(item);
            }
        }
    }
}