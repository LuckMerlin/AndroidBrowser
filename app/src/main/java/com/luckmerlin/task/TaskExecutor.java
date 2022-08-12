package com.luckmerlin.task;

import android.os.Handler;
import android.os.Looper;
import com.luckmerlin.debug.Debug;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor implements Executor{
    private final LinkedBlockingQueue<Runnable> mWaitingQueue=new LinkedBlockingQueue<>();
    private ExecutorService mExecutor;
    private boolean mFullExecuting=false;
    private final Handler mHandler=new Handler(Looper.getMainLooper());

    public TaskExecutor(){
        mExecutor=new ThreadPoolExecutor(0, 4,
                60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), (Runnable r) -> {
            Thread thread = new Thread(r);
            thread.setName("TaskExecutorTask");
            return thread;
        },(Runnable r, ThreadPoolExecutor executor)-> mFullExecuting=mWaitingQueue.offer(r)||true);
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
        if (task instanceof OnPendingExecute&&!(((OnPendingExecute)task).onPendingExecute(this))){
            return false;
        }
        executor.execute(new TaskRunnable(task,callback) {
            @Override
            public void run() {
                if (!(task instanceof OnPreExecute)||((OnPreExecute)task).onPendingExecute(TaskExecutor.this)){
                    mTask.execute(mCallback);
                }
                mFullExecuting=false;
                post(()->{
                    LinkedBlockingQueue<Runnable> waitingQueue=mWaitingQueue;//Check waiting
                    Runnable runnable=null;
                    while (!mFullExecuting&&null!=waitingQueue&&(null!=(runnable=waitingQueue.poll())) && runnable instanceof TaskRunnable){
                        TaskRunnable taskRunnable=(TaskRunnable)runnable;
                        execute(taskRunnable.mTask,taskRunnable.mCallback);
                    }
                },-1);
            }
        });
        return true;
    }

    @Override
    public List<Task> getExecuting() {
        return null;
    }

    public final boolean post(Runnable runnable,int delay){
        return null!=runnable&mHandler.post(runnable);
    }

    private static abstract class TaskRunnable implements Runnable{
        protected final Task mTask;
        protected final OnProgressChange mCallback;
        protected TaskRunnable(Task task,OnProgressChange callback){
            mTask=task;
            mCallback=callback;
        }
    }
}
