package com.luckmerlin.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.MatcherInvoker;
import com.luckmerlin.core.OnInvoke;
import com.luckmerlin.core.ParcelObject;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.luckmerlin.object.ObjectCreator;
import com.luckmerlin.task.Option;
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
    private WeakReference<Context> mContextReference;
    private final Map<Listener,Matcher<Task>> mListeners=new ConcurrentHashMap<>();
    private final Handler mHandler=new Handler(Looper.getMainLooper());
    private final ParcelObject.Parceler mParcelParser=new ParcelObject.Parceler();
    private final TaskSaver mTaskSaver;

    public TaskExecutor(Context context,TaskSaver taskSaver){
        mTaskSaver=taskSaver;
        mContextReference=null!=context?new WeakReference<>(context):null;
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
                Task task=taskRunnable.mTask;
                setStatusChange(STATUS_WAITING,taskRunnable,mListeners);
                if (null!=task&&(task instanceof OnExecuteWaiting)
                    &&((OnExecuteWaiting)task).onExecuteWaiting(TaskExecutor.this)){
                    Debug.D("Task is interrupted."+task);
                    setStatusChange(STATUS_INTERRUPTED,taskRunnable,mListeners);
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
                    updateStatusChange(STATUS_START_LOAD_SAVED,null,mListeners);
                    taskSaver.load((byte[] bytes)->executeWithTaskBytes(bytes));
                    poolExecutor.setMaximumPoolSize(maxPoolSize);
                    updateStatusChange(STATUS_FINISH_LOAD_SAVED,null,mListeners);
                    return null;
                }
            }, Option.EXECUTE);
        }
    }

    public final Context getContext(){
        WeakReference<Context> reference=mContextReference;
        return null!=reference?reference.get():null;
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
                    Boolean matched=null!=matcher?null!=data?!isInnerTask(data.mTask)&&matcher.match(data.mTask):null:true;
                    if (null!=matched&&matched){
                        updateStatusChange(data.getStatus(),data.mTask,(OnStatusChangeListener)listener);
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

    @Override
    public boolean execute(Object taskObj, int optionArg) {
        return execute(taskObj,optionArg,false);
    }

    private boolean execute(Object taskObj,final int optionArg,boolean fromSaved){
        ExecutorService executor=mExecutor;
        if (null==executor){
            Debug.E("Fail execute task while executor is invalid.");
            return false;
        }else if (null==taskObj){
            return false;
        }else if (!(taskObj instanceof Task)){
            return false;
        }
        final Task task=(Task)taskObj;
        final boolean innerTask=isInnerTask(task);
        ExecuteTask executeTask=findFirst((ExecuteTask data)-> null!=data&&data.isTask(task)?true:false);
        if (null==executeTask){
            executeTask=new ExecuteTask(this,task,getContext(), optionArg,mHandler, fromSaved);
            mQueue.add(executeTask);
            if (!innerTask){
                setStatusChange(STATUS_ADD,executeTask,mListeners);
            }
        }
        executeTask.setOption(Option.isOptionEnabled(optionArg,Option.RESET)?(optionArg&~Option.RESET): executeTask.getOption()|optionArg);
        //Check cancel
        if (Option.isOptionEnabled(optionArg, Option.CANCEL)){
            //Do nothing
        }
        //Check save
        if (!innerTask){
            if (Option.isOptionEnabled(optionArg,Option.NOT_SAVE)){
                deleteSaveTask(executeTask);
            }else{
                boolean saved=saveTask(task,optionArg);
                executeTask.mSaved=saved;
            }
        }
        //Check pending
        if (!Option.isOptionEnabled(optionArg, Option.PENDING)){
            return true;
        }else if (task instanceof OnExecutePending &&(((OnExecutePending)task).onExecutePending(this))){
            return true;
        }
        if (executeTask.isRunning()){
            return true;
        }
        //Check execute
        if (!Option.isOptionEnabled(optionArg, Option.EXECUTE)){
            return true;
        }
        //Clean cancel option while execute
        executeTask.setOption(Option.enableOption(executeTask.getOption(),Option.CANCEL,false));
        Debug.D("Pending execute task."+task);
        setStatusChange(STATUS_PENDING,executeTask,mListeners);
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
        match(mQueue,collector);
        return collector.getFirstMatched();
    }

    private boolean deleteSaveTask(ExecuteTask executeTask){
        if (null!=executeTask){
            TaskSaver taskSaver=mTaskSaver;
            boolean succeed=null!=taskSaver&&taskSaver.delete(executeTask.mTask);
            if (executeTask.isBackgroundEnabled()){
                removeFromQueue(executeTask);
                updateStatusChange(STATUS_REMOVE,executeTask.mTask,mListeners);
            }
            return succeed;
        }
        return false;
    }

    private Task executeWithTaskBytes(byte[] taskBytes){
        if (null==taskBytes){
            return null;
        }
        Parcel parcel=Parcel.obtain();
        parcel.unmarshall(taskBytes,0,taskBytes.length);
        parcel.setDataPosition(0);
        int option=parcel.readInt();
        parcel.readString();//Version
        ParcelObject parcelObject=mParcelParser.read(parcel);
        parcel.recycle();
        if (null==parcelObject){
            return null;
        }else if (!(parcelObject instanceof Task)){
            return null;
        }
        Task task=(Task)parcelObject;
        return TaskExecutor.this.execute(task,option)?task:null;
    }

    private boolean saveTask(Task task,int option){
        TaskSaver taskSaver=null!=task?mTaskSaver:null;
        if (null!=taskSaver&&task instanceof ParcelObject){
            Parcel parcel=Parcel.obtain();
            parcel.setDataPosition(0);
            parcel.writeInt(option);
            parcel.writeString("");//Version
            mParcelParser.write(parcel,task);
            byte[] bytes=parcel.marshall();
            boolean succeed=taskSaver.write(task,bytes);
            parcel.recycle();
            return succeed;
        }
        return false;
    }

    @Override
    public void findTask(OnTaskFind onTaskFind) {
        match(mQueue,(ExecuteTask data)-> null!=data&&!isInnerTask(data.mTask)&&
                onTaskFind.onTaskFind(data.mTask,data.getStatus(),data.getOption())?null:false);
    }

    public final boolean post(Runnable runnable, int delay){
        return null!=runnable&mHandler.post(runnable);
    }

    public static boolean isUiThread(){
        Looper looper=Looper.myLooper();
        Looper uiLooper=Looper.getMainLooper();
        return null!=looper&&null!=uiLooper&&looper==uiLooper;
    }

    private class ExecuteTask extends Runtime implements Runnable{
        protected final Task mTask;
        protected final boolean mFromSaved;
        protected final Executor mExecutor;
        private boolean mSaved;

        protected ExecuteTask(Executor executor, Task task, Context context,int option, Handler handler, boolean fromSaved){
            super(option,handler,context);
            mExecutor=executor;
            mTask=task;
            mFromSaved=fromSaved;
        }

        @Override
        public void run() {
            setStatusChange(STATUS_EXECUTING,this,mListeners);
            if (!(mTask instanceof OnExecuteStart)||!((OnExecuteStart)mTask).onExecuteStart(TaskExecutor.this)){
                mTask.execute(this, (Task task, Progress progress)->
                iterateListener(task,(Listener data)->{
                    if (null!=data&&data instanceof OnProgressChange){
                        ((OnProgressChange)data).onProgressChanged(task,progress);
                    }
                    return false;
                }));
            }
            mFullExecuting=false;
            setStatusChange(STATUS_FINISH,this,mListeners);
            boolean deleteSucceed=false;
            if (mTask instanceof OnExecuteFinish&&((OnExecuteFinish)mTask).onExecuteFinish(TaskExecutor.this)){
                deleteSucceed=true;
            }
            deleteSucceed=deleteSucceed||Option.isOptionEnabled(getOption(),Option.DELETE_SUCCEED);
            Progress progress=null;
            if (deleteSucceed&&null!=(progress=mTask.getProgress())&&progress.isSucceed()){
                deleteSaveTask(this);
            }else if (!Option.isOptionEnabled(getOption(),Option.NOT_SAVE)){
                saveTask(mTask,getOption());
            }else if (mSaved){
                deleteSaveTask(this);
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
                        execute(taskRunnable.mTask,taskRunnable.getOption(),taskRunnable.mFromSaved);
                    }
                }
            },-1);
        }

        private boolean isBackgroundEnabled(){
            return Option.isOptionEnabled(getOption(),Option.BACKGROUND);
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
    }

    private void setStatusChange(int status,ExecuteTask task,Map<Listener,Matcher<Task>> listeners){
        if (null!=task){
            task.setStatus(status);
        }
        updateStatusChange(status,null!=task?task.mTask:null,listeners);
    }

    private void updateStatusChange(int status,Task task,Map<Listener,Matcher<Task>> listeners){
        Set<Listener> set=null!=listeners&&!isInnerTask(task)?listeners.keySet():null;
        if (null!=set){
            Matcher<Task> matcher=null;Boolean matched=null;
            for (Listener listener:set) {
                if (null!=listener&&listener instanceof OnStatusChangeListener&&null!=
                        (matcher=listeners.get(listener))&&null!=
                        (matched=matcher.match(task))&&matched){
                    updateStatusChange(status,task,(OnStatusChangeListener)listener);
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

    private void updateStatusChange(int status,Task task,OnStatusChangeListener listener){
        if (null==listener||isInnerTask(task)){
            return;
        }else if (listener instanceof UiListener&&!isUiThread()){
            post(()->updateStatusChange(status,task,listener),-1);
            return;
        }
        listener.onStatusChanged(status,task,TaskExecutor.this);
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
