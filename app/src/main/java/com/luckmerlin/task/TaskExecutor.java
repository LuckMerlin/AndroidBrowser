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
    private final List<ExecuteTask> mQueue=new CopyOnWriteArrayList<>();
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
                if (null!=task&&(task instanceof OnExecuteWaiting)
                    &&((OnExecuteWaiting)task).onExecuteWaiting(TaskExecutor.this)){
                    taskRunnable.setStatus(ExecuteTask.STATUS_INTERRUPTED);
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
        final ExecuteTask[] existTask=new ExecuteTask[1];
        match((ExecuteTask data)-> null!=data&&data.isTask(task)&&null!=(existTask[0]=data));
        ExecuteTask executeTask=existTask[0];
        if (null==executeTask){
            executeTask=new ExecuteTask(task,callback) {
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
                        List<ExecuteTask> waitingQueue=mQueue;//Check waiting
                        ExecuteTask taskRunnable=null;
                        int size=null!=waitingQueue?waitingQueue.size():-1;
                        for (int i = 0; i < size; i++) {
                            if (mFullExecuting){
                                break;
                            }else if (null!=(taskRunnable=waitingQueue.get(i))&& taskRunnable.isStatus(STATUS_WAITING)){
                                execute(taskRunnable.mTask,taskRunnable.mCallback);
                            }
                        }
                    },-1);
                }
            }.setStatus(ExecuteTask.STATUS_PENDING);
            mQueue.add(executeTask);
        }
        //
        if (!executeTask.isStatus(ExecuteTask.STATUS_PENDING,ExecuteTask.
                STATUS_INTERRUPTED,ExecuteTask.STATUS_FINISH)){
            Debug.E("Fail execute task while task status is not need execute.status="+executeTask.mStatus);
            return false;
        }
        executor.execute(executeTask.setStatus(ExecuteTask.STATUS_PENDING));
        return true;
    }

    @Override
    public void match(Matcher<ExecuteTask> matcher) {
        match(mQueue,matcher);
    }

    public final boolean post(Runnable runnable, int delay){
        return null!=runnable&mHandler.post(runnable);
    }

    public static abstract class ExecuteTask implements Runnable{
        public final static int STATUS_PENDING=2000;
        public final static int STATUS_EXECUTING=2001;
        public final static int STATUS_WAITING=2002;
        public final static int STATUS_FINISH=2003;
        public final static int STATUS_INTERRUPTED=2004;
        protected final Task mTask;
        protected final OnProgressChange mCallback;
        private int mStatus=STATUS_PENDING;

        protected ExecuteTask(Task task,OnProgressChange callback){
            mTask=task;
            mCallback=callback;
        }

        public final boolean isTask(Task task){
            Task current=mTask;
            return null!=current&&null!=task&&current==task;
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

        protected final ExecuteTask setStatus(int status){
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
