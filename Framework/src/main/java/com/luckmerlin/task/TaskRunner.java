package com.luckmerlin.task;

import com.luckmerlin.data.Parcelable;
import com.luckmerlin.utils.Utils;

abstract class TaskRunner implements Runnable{
    private String mTaskId;
    private int mStatus=Executor.STATUS_IDLE;
    private int mOption;

    public String getTaskId() {
        return mTaskId;
    }

    public TaskRunner setTaskId(String id){
        mTaskId=id;
        return this;
    }

    public TaskRunner setOption(int option) {
        this.mOption = option;
        return this;
    }

    public int getStatus() {
        return mStatus;
    }

    public boolean isAnyStatus(int... status){
        for (int i = 0; i < status.length; i++) {
            if (status[i]==mStatus){
                return true;
            }
        }
        return false;
    }

    public final boolean isNeedDelete(){
        Task task=getTask();
        String taskId=mTaskId;
        return null!=task&&task instanceof Parcelable &&Option.
                isOptionEnabled(mOption,Option.DELETE)&&null!=taskId&&taskId.length()>0;
    }

    public final boolean isTaskIdEquals(String taskId){
        return Utils.isEqualed(mTaskId,taskId,true);
    }

    protected abstract void onStatusChanged(int last,int current);

    public TaskRunner setStatus(int status,boolean notify){
        int last=mStatus;
        mStatus=status;
        if (notify){
            onStatusChanged(last,status);
        }
        return this;
    }

    protected abstract Task getTask();

    public boolean isTaskEquals(Object obj){
        Task task=getTask();
        return null!=task&&null!=obj&&task.equals(obj);
    }

    public int getOption() {
        return mOption;
    }

    @Override
    public void run() {
        Task task=getTask();
        if (null==task){
            return;
        }
        setStatus(Executor.STATUS_EXECUTING,true);
        task.execute(null, (Task task1)-> {

        });
        setStatus(Executor.STATUS_FINISH,true);
    }
}
