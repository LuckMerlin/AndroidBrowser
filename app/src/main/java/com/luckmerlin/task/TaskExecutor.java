package com.luckmerlin.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.MatcherInvoker;
import com.luckmerlin.core.Result;
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
    private Listener mListener;
    private final Handler mHandler=new Handler(Looper.getMainLooper());
    private final TaskSaver mTaskSaver;

    public TaskExecutor(){
        this(null);
    }

    public TaskExecutor(TaskSaver taskSaver){
        mTaskSaver=taskSaver;
        final int maxPoolSize=4;
        ThreadPoolExecutor poolExecutor=new ThreadPoolExecutor(0, maxPoolSize,
                60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), (Runnable r) -> {
            Thread thread = new Thread(r);
            thread.setName("TaskExecutorTask");
            return thread;
        },(Runnable r, ThreadPoolExecutor executor)-> {
            mFullExecuting=true;
            if (null!=r&&r instanceof ExecuteTask){
                ExecuteTask taskRunnable=(ExecuteTask)r;
                taskRunnable.setStatus(STATUS_WAITING);
                Task task=taskRunnable.mTask;
                notifyStatusChange(STATUS_WAITING,task,mListener);
                if (null!=task&&(task instanceof OnExecuteWaiting)
                    &&((OnExecuteWaiting)task).onExecuteWaiting(TaskExecutor.this)){
                    taskRunnable.setStatus(STATUS_INTERRUPTED);
                    notifyStatusChange(STATUS_INTERRUPTED,task,mListener);
                }
            }
        });
        mExecutor=poolExecutor;
        if (null!=taskSaver){
            poolExecutor.setMaximumPoolSize(1);
            execute(new InnerTask(){
                @Override
                protected Result onExecute() {
                    notifyStatusChange(STATUS_START_LOAD_SAVED,null,mListener);
                    taskSaver.load((Task data)-> null!=data&&TaskExecutor.this.
                            execute(data,true,null)&&false);
                    poolExecutor.setMaximumPoolSize(maxPoolSize);
                    notifyStatusChange(STATUS_FINISH_LOAD_SAVED,null,mListener);
                    return null;
                }
            });
        }
    }

    @Override
    public final Executor setListener(Listener listener) {
        mListener=listener;
        return this;
    }

    public final boolean execute(Task task){
        return execute(task,null);
    }

    public final boolean execute(Task task,OnProgressChange callback){
        return execute(task,false,callback);
    }

    public final boolean remove(Task task,boolean interrupt){
//        OnTaskAddListener listener=mAddListener;
//        if (null!=listener){
//            listener.onTaskAdd(task,fromSaved);
//        }
        return false;
    }

    private final boolean execute(Task task,boolean fromSaved,OnProgressChange callback){
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
                    setStatus(STATUS_EXECUTING);
                    notifyStatusChange(STATUS_EXECUTING,task,mListener);
                    if (!(task instanceof OnExecuteStart)||!((OnExecuteStart)task).
                            onExecuteStart(TaskExecutor.this)){
                        mTask.execute(mCallback);
                    }
                    setStatus(STATUS_FINISH);
                    mFullExecuting=false;
                    if (task instanceof OnExecuteFinish&&((OnExecuteFinish)task).onExecuteFinish(TaskExecutor.this)){
                        //Do nothing
                    }
                    notifyStatusChange(STATUS_FINISH,task,mListener);
                    if (task instanceof Parcelable &&(!(task instanceof OnTaskSave)||
                            ((OnTaskSave)task).onTaskSave(TaskExecutor.this))){
                        save(task);
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
            }.setStatus(STATUS_PENDING);
            if (fromSaved){
                mQueue.add(0,executeTask);
            }else{
                mQueue.add(executeTask);
            }
            notifyStatusChange(STATUS_ADD,task,mListener);
        }
        //
        if (!executeTask.isStatus(STATUS_PENDING, STATUS_INTERRUPTED,STATUS_FINISH,STATUS_WAITING)){
            Debug.E("Fail execute task while task status is not need execute.status="+executeTask.mStatus);
            return false;
        }
        Debug.D("Pending execute task."+task);
        notifyStatusChange(STATUS_PENDING,task,mListener);
        executor.execute(executeTask.setStatus(STATUS_PENDING));
        return true;
    }

    private boolean save(Task task){
        TaskSaver taskSaver=null!=task?mTaskSaver:null;
        return null!=taskSaver&&taskSaver.save(task);
    }

    @Override
    public void match(Matcher<ExecuteTask> matcher) {
        match(mQueue,null!=matcher?(ExecuteTask data)->
                null!=data&&!isInnerTask(data.mTask)?matcher.match(data):false:null);
    }

    public final boolean post(Runnable runnable, int delay){
        return null!=runnable&mHandler.post(runnable);
    }

    public static boolean isUiThread(){
        Looper looper=Looper.myLooper();
        Looper uiLooper=Looper.getMainLooper();
        return null!=looper&&null!=uiLooper&&looper==uiLooper;
    }

    public static abstract class ExecuteTask implements Runnable{
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

    private void notifyStatusChange(int status,Task task,Listener listener){
        if (null!=listener&&!isInnerTask(task)){
            if (listener instanceof OnAddRemoveChangeListener&&(status==STATUS_ADD||status==STATUS_REMOVE)){
                OnAddRemoveChangeListener changeListener=((OnAddRemoveChangeListener)listener);
                if (isUiThread()){
                    changeListener.onAddRemoveChanged(status,task,TaskExecutor.this);
                }else{
                    post(()->changeListener.onAddRemoveChanged(status,task,TaskExecutor.this),0);
                }
            }
        }
    }

    private boolean isInnerTask(Task task){
        return null!=task&&task instanceof InnerTask;
    }

    private static abstract class InnerTask extends AbstractTask {
        public InnerTask() {
            super(null);
        }
    }
}
