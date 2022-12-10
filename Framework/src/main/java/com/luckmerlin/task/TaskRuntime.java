package com.luckmerlin.task;

import com.luckmerlin.data.Parcelable;
import com.luckmerlin.utils.Utils;

 abstract class TaskRuntime extends Runtime implements Runnable {
    private Task mTask;
    private String mTaskId;

    public TaskRuntime(Task task,TaskExecutor executor){
        super(executor);
        mTask=task;
    }

    public String getTaskId() {
        return mTaskId;
    }

    public final TaskRuntime setTaskId(String id){
        mTaskId=id;
        return this;
    }

    public final boolean isNeedDelete(){
        Task task=getTask();
        String taskId=mTaskId;
        return null!=task&&task instanceof Parcelable &&Option.
                isOptionEnabled(getOption(),Option.DELETE)&&null!=taskId&&taskId.length()>0;
    }

    protected final Task getTask(){
        return mTask;
    }

    @Override
    public void run() {
        Task task=getTask();
        if (null==task){
            return;
        }
        setStatus(Executor.STATUS_EXECUTING,true);
        Executor executor=getExecutor();
        TaskExecutor taskExecutor=null!=executor&&executor instanceof TaskExecutor?
                ((TaskExecutor)executor):null;
        task.execute(this,null!=taskExecutor?(Task task1)->
                taskExecutor.notifyUpdateChangeToAll(this):null);
        setStatus(Executor.STATUS_FINISH,true);
    }

    public final boolean isTaskIdEquals(String taskId){
        return Utils.isEqualed(mTaskId,taskId,true);
    }

    public boolean isTaskEquals(Object obj){
        Task task=getTask();
        return null!=task&&null!=obj&&task.equals(obj);
    }
 }
