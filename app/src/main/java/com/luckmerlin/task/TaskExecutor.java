package com.luckmerlin.task;

import android.os.Handler;
import android.os.Looper;

import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.MatcherInvoker;
import com.luckmerlin.debug.Debug;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor extends MatcherInvoker implements Executor{
    private final List<ExecuteTask> mWaitingQueue=new CopyOnWriteArrayList<>();
    private ExecutorService mExecutor;
    private boolean mFullExecuting=false;
    private final Handler mHandler=new Handler(Looper.getMainLooper());

    public TaskExecutor(){
        mExecutor=new ThreadPoolExecutor(0, 4,
                60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), (Runnable r) -> {
            Thread thread = new Thread(r);
            thread.setName("TaskExecutorTask");
            return thread;
        },(Runnable r, ThreadPoolExecutor executor)-> {
            mFullExecuting=true;
            if (null!=r&&r instanceof ExecuteTask){
                ExecuteTask taskRunnable=(ExecuteTask)r;
                taskRunnable.setStatus(ExecuteTask.STATUS_WAITING);
                Task task=taskRunnable.mTask;
                if (null==task||!(task instanceof OnExecuteWaiting)
                    ||!((OnExecuteWaiting)task).onExecuteWaiting(TaskExecutor.this)){
                    mWaitingQueue.add(taskRunnable);
                }
            }
        });
    }

    public final boolean execute(Task task){
        return execute(task,null);
    }

    public final boolean execute(Task task,OnProgressChange callback){
        if (null==task){
            Debug.E("Fail execute task while task is invalid.");
            return false;
        }
        ExecutorService executor=mExecutor;
        if (null==executor){
            Debug.E("Fail execute task while executor is invalid.");
            return false;
        }
        if (task instanceof OnExecutePending &&(((OnExecutePending)task).onExecutePending(this))){
            return false;
        }
        executor.execute(new ExecuteTask(task,callback) {
            @Override
            public void run() {
                setStatus(ExecuteTask.STATUS_EXECUTING);
                if (!(task instanceof OnExecuteStart)||!((OnExecuteStart)task).onExecuteStart(TaskExecutor.this)){
                    mTask.execute(mCallback);
                }
                setStatus(ExecuteTask.STATUS_FINISH);
                mFullExecuting=false;
                if (task instanceof OnExecuteFinish&&((OnExecuteFinish)task).onExecuteFinish(TaskExecutor.this)){
                    //Do nothing
                }
                post(()->{
                    List<ExecuteTask> waitingQueue=mWaitingQueue;//Check waiting
                    ExecuteTask taskRunnable=null;
                    while (!mFullExecuting&&null!=waitingQueue&&waitingQueue.size()>0&& (null!=(taskRunnable=waitingQueue.get(0)))){
                        execute(taskRunnable.mTask,taskRunnable.mCallback);
                    }
                },-1);
            }
        }.setStatus(ExecuteTask.STATUS_PENDING));
        return true;
    }

    @Override
    public void match(Matcher<ExecuteTask> matcher) {
        match(mWaitingQueue,matcher);
    }

    public final boolean post(Runnable runnable, int delay){
        return null!=runnable&mHandler.post(runnable);
    }

    public static abstract class ExecuteTask implements Runnable{
        public final static int STATUS_PENDING=2000;
        public final static int STATUS_EXECUTING=2001;
        public final static int STATUS_WAITING=2002;
        public final static int STATUS_FINISH=2003;
        protected final Task mTask;
        protected final OnProgressChange mCallback;
        private int mStatus=STATUS_PENDING;

        protected ExecuteTask(Task task,OnProgressChange callback){
            mTask=task;
            mCallback=callback;
        }

        public final boolean isStatus(int... status) {
            if (null != status) {
                for (int i = 0; i < status.length; i++) {
                    if (status[i]==mStatus){
                        return true;
                    }
                }
            }
            return false;
        }

        public final ExecuteTask setStatus(int status){
            mStatus=status;
            return this;
        }

        public final Task getTask() {
            return mTask;
        }

        public final OnProgressChange getOnProgressChange() {
            return mCallback;
        }
    }
}
