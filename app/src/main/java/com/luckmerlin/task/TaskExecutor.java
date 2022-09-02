package com.luckmerlin.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.MatcherInvoker;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor extends MatcherInvoker implements Executor{
    private final List<ExecuteTask> mQueue=new CopyOnWriteArrayList<>();
    private ExecutorService mExecutor;
    private boolean mFullExecuting=false;
    private final Map<Listener,Matcher<Task>> mListeners=new ConcurrentHashMap<>();
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
                notifyStatusChange(STATUS_WAITING,task,mListeners);
                if (null!=task&&(task instanceof OnExecuteWaiting)
                    &&((OnExecuteWaiting)task).onExecuteWaiting(TaskExecutor.this)){
                    Debug.D("Task is interrupted."+task);
                    taskRunnable.setStatus(STATUS_INTERRUPTED);
                    notifyStatusChange(STATUS_INTERRUPTED,task,mListeners);
                }else {
                    Debug.D("Task is waiting."+task);
                }
            }
        });
        mExecutor=poolExecutor;
        if (null!=taskSaver){
            poolExecutor.setMaximumPoolSize(1);
            execute(new InnerTask(){
                @Override
                public Result execute(Runtime runtime, OnProgressChange callback) {
                    notifyStatusChange(STATUS_START_LOAD_SAVED,null,mListeners);
                    taskSaver.load((byte[] bytes)->executeWithTaskBytes(bytes));
                    poolExecutor.setMaximumPoolSize(maxPoolSize);
                    notifyStatusChange(STATUS_FINISH_LOAD_SAVED,null,mListeners);
                    return null;
                }
            },Option.NONE);
        }
    }

    @Override
    public Executor putListener(Listener listener, Matcher<Task> matcher,boolean notify) {
        if (null!=listener){
            Map<Listener,Matcher<Task>> listeners=mListeners;
            if (null!=listeners){
                listeners.put(listener,null!=matcher?matcher:(Task data)-> true);
            }
            if (notify&&listener instanceof OnStatusChangeListener){
                match(mQueue,(ExecuteTask data)-> {
                        Boolean matched=null!=matcher&&null!=data?matcher.match(data.mTask):null;
                        if (null!=matched&&matched){
                            notifyStatusChange(data.getStatus(),data.mTask,(OnStatusChangeListener)listener);
                        }
                        return matched;
                });
            }
        }
        return this;
    }

    @Override
    public Executor removeListener(Listener listener) {
        Map<Listener,Matcher<Task>> listeners=mListeners;
        if (null!=listener&&null!=listeners){
            listeners.remove(listener);
        }
        return this;
    }

    public final boolean execute(Task task,int option){
        return execute(task,option,null);
    }

    @Override
    public boolean execute(Object task,int option, OnProgressChange callback) {
        if (null==task){
            return false;
        }else if (task instanceof Task){
            return execute((Task) task,option,false,callback);
        }
        return false;
    }

    @Override
    public boolean option(Object task, int option) {
        if (null==task){
            return false;
        }else if (task instanceof Task){
            ExecuteTask executeTask=findFirst((Task) task);
            if (null==executeTask){
                return false;
            }else if(!executeTask.setOption(option)){
                return false;
            }
            boolean succeed=true;
            if (executeTask.isRemoveEnabled()){
                succeed&=removeFromQueue(executeTask);
            }
            if(executeTask.isDeleteEnabled()){
                succeed&=deleteSaveTask((Task)task);
            }
            return succeed;
        }
        return false;
    }

    private final boolean execute(Task task,int option,boolean fromSaved,OnProgressChange callback){
        if (null==task){
            Debug.E("Fail execute task while task is invalid.");
            return false;
        }
        ExecuteTask currentExecuteTask=findFirst((ExecuteTask data)-> null!=data&&data.isTask(task)?true:false);
        if (null!=currentExecuteTask){
            if (currentExecuteTask.isRunning()){
                Debug.E("Fail execute task while task is running."+task);
                return false;
            }
            fromSaved=currentExecuteTask.mFromSaved;
        }
        ExecutorService executor=mExecutor;
        if (null==executor){
            Debug.E("Fail execute task while executor is invalid.");
            return false;
        }
        final boolean needSave=task instanceof Parcelable &&(!(task instanceof OnTaskSave)||
                ((OnTaskSave)task).onTaskSave(TaskExecutor.this));
        if (!fromSaved&&needSave&&!Runtime.isDeleteEnabled(option)){
            saveTask(task,option);
        }
        if (task instanceof OnExecutePending &&(((OnExecutePending)task).onExecutePending(this))){
            return false;
        }
        final ExecuteTask[] existTask=new ExecuteTask[1];
        match((ExecuteTask data)-> null!=data&&data.isTask(task)&&null!=(existTask[0]=data));
        ExecuteTask executeTask=existTask[0];
        if (null==executeTask){
            executeTask=new ExecuteTask(this,task, option, fromSaved,callback) {
                @Override
                public void run() {
                    setStatus(STATUS_EXECUTING);
                    notifyStatusChange(STATUS_EXECUTING,task,mListeners);
                    if (!(task instanceof OnExecuteStart)||!((OnExecuteStart)task).
                            onExecuteStart(TaskExecutor.this)){
                        OnProgressChange progressChange=mCallback;
                        mTask.execute(this, (Task task, Progress progress)-> {
                            iterateListener(task,(Listener data)->{
                                if (null!=data&&data instanceof OnProgressChange){
                                    ((OnProgressChange)data).onProgressChanged(task,progress);
                                }
                                return false;
                            });
                            if (null!=progressChange){
                                progressChange.onProgressChanged(task,progress);
                            }
                        });
                    }
                    setStatus(STATUS_FINISH);
                    mFullExecuting=false;
                    if (task instanceof OnExecuteFinish&&((OnExecuteFinish)task).onExecuteFinish(TaskExecutor.this)){
                        //Do nothing
                    }
                    notifyStatusChange(STATUS_FINISH,task,mListeners);
                    if (needSave&&!isDeleteEnabled()){
                        saveTask(task,option);
                    }
                    post(()->{
                        List<ExecuteTask> waitingQueue=mQueue;//Check waiting
                        ExecuteTask taskRunnable=null;
                        int size=null!=waitingQueue?waitingQueue.size():-1;
                        for (int i = 0; i < size; i++) {
                            if (mFullExecuting){
                                break;
                            }else if (null!=(taskRunnable=waitingQueue.get(i))&& taskRunnable.isStatus(STATUS_WAITING)){
                                Debug.D("Task to execute again."+taskRunnable.mTask);
                                execute(taskRunnable.mTask,taskRunnable.getOption(),taskRunnable.mFromSaved,taskRunnable.mCallback);
                            }
                        }
                    },-1);
                }
            };
            executeTask.setStatus(STATUS_PENDING);
            if (fromSaved){
                mQueue.add(0,executeTask);
            }else{
                mQueue.add(executeTask);
            }
            notifyStatusChange(STATUS_ADD,task,mListeners);
        }
        //
        if (!executeTask.isStatus(STATUS_PENDING, STATUS_INTERRUPTED,STATUS_FINISH,STATUS_WAITING)){
            Debug.E("Fail execute task while task status is not need execute.status="+executeTask.getStatus());
            return false;
        }
        Debug.D("Pending execute task."+task);
        notifyStatusChange(STATUS_PENDING,task,mListeners);
        executeTask.setStatus(STATUS_PENDING);
        executor.execute(executeTask);
        return true;
    }

    private boolean removeFromQueue(ExecuteTask executeTask){
        List<ExecuteTask> queue=mQueue;
        return null!=executeTask&&null!=queue&&queue.remove(executeTask);
    }

    private ExecuteTask findFirst(Task task){
        return null!=task?findFirst((ExecuteTask data)->null!=data&&data.isTask(task)):null;
    }

    private ExecuteTask findFirst(Matcher<ExecuteTask> matcher){
        MatchedCollector<ExecuteTask> collector=new MatchedCollector<ExecuteTask>(1).setMatcher(matcher);
        match(collector);
        return collector.getFirstMatched();
    }

    private Task executeWithTaskBytes(byte[] taskBytes){
        Parcel parcel=Parcel.obtain();
        parcel.unmarshall(taskBytes,0,taskBytes.length);
        parcel.setDataPosition(0);
        int option=parcel.readInt();
        Parcelable parcelable=parcel.readParcelable(getClass().getClassLoader());
        parcel.recycle();
        if (null==parcelable||!(parcelable instanceof Task)){
            return null;
        }
        Task task=(Task)parcelable;
        return TaskExecutor.this.execute(task,option,null)?task:null;
    }

    private boolean deleteSaveTask(Task task){
        TaskSaver taskSaver=null!=task?mTaskSaver:null;
        return null!=taskSaver&&taskSaver.delete(task);
    }

    private boolean saveTask(Task task,int option){
        TaskSaver taskSaver=null!=task?mTaskSaver:null;
        if (null!=taskSaver&&task instanceof Parcelable){
            Parcel parcel=Parcel.obtain();
            parcel.setDataPosition(0);
            parcel.writeInt(option);
            parcel.writeParcelable((Parcelable)task,0);
            byte[] bytes=parcel.marshall();
            boolean succeed=taskSaver.write(task,bytes);
            parcel.recycle();
            return succeed;
        }
        return false;
    }

    @Override
    public void match(Matcher<ExecuteTask> matcher) {
        match(mQueue,null!=matcher?(ExecuteTask data)->
                null!=data&&!isInnerTask(data.mTask)?matcher.match(data):(Boolean)false:null);
    }

    public final boolean post(Runnable runnable, int delay){
        return null!=runnable&mHandler.post(runnable);
    }

    public static boolean isUiThread(){
        Looper looper=Looper.myLooper();
        Looper uiLooper=Looper.getMainLooper();
        return null!=looper&&null!=uiLooper&&looper==uiLooper;
    }

    public static abstract class ExecuteTask extends Runtime implements Runnable{
        protected final Task mTask;
        protected final boolean mFromSaved;
        protected final OnProgressChange mCallback;
        protected final Executor mExecutor;

        protected ExecuteTask(Executor executor,Task task,int option,boolean fromSaved,OnProgressChange callback){
            super(option);
            mExecutor=executor;
            mTask=task;
            mFromSaved=fromSaved;
            mCallback=callback;
        }

        @Override
        public Executor getExecutor() {
            return mExecutor;
        }

        public final boolean isTask(Task task){
            Task current=mTask;
            return null!=current&&null!=task&&current==task;
        }

        public final Task getTask() {
            return mTask;
        }

        public final OnProgressChange getOnProgressChange() {
            return mCallback;
        }
    }

    private void notifyStatusChange(int status,Task task,Map<Listener,Matcher<Task>> listeners){
        Set<Listener> set=null!=listeners&&!isInnerTask(task)?listeners.keySet():null;
        if (null!=set){
            Matcher<Task> matcher=null;Boolean matched=null;
            for (Listener listener:set) {
                if (null!=listener&&listener instanceof OnStatusChangeListener&&null!=
                        (matcher=listeners.get(listener))&&null!=
                        (matched=matcher.match(task))&&matched){
                    notifyStatusChange(status,task,(OnStatusChangeListener)listener);
                }
            }
        }
    }

    private void iterateListener(Task task,Matcher<Listener> listenerMatcher){
        if (null==listenerMatcher){
            return;
        }
        Map<Listener,Matcher<Task>> listeners=mListeners;
        Set<Listener> set=null!=listeners&&!isInnerTask(task)?listeners.keySet():null;
        if (null==set) {
            return;
        }
        Matcher<Task> matcher = null;
        Boolean matched = null;
        for (Listener listener : set) {
            if (null!=(matcher=listeners.get(listener))&&null!=(matched=matcher.match(task))&&matched){
                if (null==listenerMatcher.match(listener)) {
                    break;
                }
            }
        }
    }

    private void notifyStatusChange(int status,Task task,OnStatusChangeListener listener){
        if (null==listener){
            return;
        }else if (listener instanceof UiListener&&!isUiThread()){
            post(()->notifyStatusChange(status,task,listener),-1);
            return;
        }
        OnStatusChangeListener changeListener=((OnStatusChangeListener)listener);
        changeListener.onStatusChanged(status,task,TaskExecutor.this);
    }

    private boolean isInnerTask(Task task){
        return null!=task&&task instanceof InnerTask;
    }

    private static abstract class InnerTask implements Task {
        @Override
        public String getName() {
            return "Inner task.";
        }

        @Override
        public Progress getProgress() {
            return null;
        }

        @Override
        public Result getResult() {
            return null;
        }
    }

}
