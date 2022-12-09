package com.luckmerlin.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import com.luckmerlin.data.Parcelable;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.data.Parceler;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.utils.Utils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor implements Executor{
    private final List<TaskRuntime> mQueue=new CopyOnWriteArrayList<>();
    private final ExecutorService mExecutor;
    private WeakReference<Context> mContextReference;
    private final Map<Listener,Matcher<Task>> mListeners=new ConcurrentHashMap<>();
    private final Handler mHandler=new Handler(Looper.getMainLooper());
    private final Parceler mParceler=new Parceler();
    private final TaskSaver mTaskSaver;
    private boolean mExecutorFull=false;

    public TaskExecutor(Context context,TaskSaver taskSaver){
        mTaskSaver=taskSaver;
        mContextReference=null!=context?new WeakReference<>(context):null;
        int maxPoolSize=4;
        ExecutorService executor=mExecutor=new ThreadPoolExecutor(0, maxPoolSize,
                60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), (Runnable r) -> {
            Thread thread = new Thread(r);
            thread.setName("TaskExecutorTask");
            return thread;
        },(Runnable r, ThreadPoolExecutor ex)-> {
            mExecutorFull=true;
            if (null!=r&&r instanceof TaskRuntime){
               ((TaskRuntime)r).setStatus(STATUS_WAITING,true);
            }
        });
        if (null!=taskSaver){
            executor.submit(()->taskSaver.load((String taskId, byte[] bytes)->executeSavedTask(taskId,bytes,true)));
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
            List<TaskRuntime> queue=notify?mQueue:null;
            if (null!=queue){
                for (TaskRuntime runner:queue) {
                    notifyStatusChange(runner,matcher,listener);
                }
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
        return execute(taskObj,optionArg,null);
    }

    private boolean execute(Object taskObj,int option,String taskId){
        if (null==taskObj){
            Debug.E("Fail execute task while task null.");
            return false;
        }else if (!(taskObj instanceof Task)){
            Debug.E("Fail execute task while not task.");
            return false;
        }
        TaskRuntime taskRuntime=findTaskData((Task)taskObj,true);
        if (null==taskRuntime){
            Debug.E("Fail execute task while find task data fail.");
            return false;
        }else if (null!=taskId&&taskId.length()>0){
            taskRuntime.setTaskId(taskId);
        }
        taskRuntime.setOption(option);
        return execute(taskRuntime);
    }

    private boolean execute(TaskRuntime runner){
        ExecutorService executor=mExecutor;
        if (null==executor){
            Debug.E("Fail execute runner while executor is invalid.");
            return false;
        }
        Task task=null!=runner?runner.getTask():null;
        if (null==task){
            Debug.E("Fail execute runner while runner is invalid.");
            return false;
        }
        final int option=runner.getOption();
        final boolean isNeedDelete= runner.isNeedDelete();
        final TaskSaver taskSaver=mTaskSaver;
        final String taskId=runner.getTaskId();
        if (isNeedDelete&&null!=taskSaver&&null!=taskId&&taskId.length()>0){//To delete task while need delete
            Debug.D("To delete task."+taskId);
            taskSaver.delete(taskId);//Try to delete saved task
        }
        if (runner.isAnyStatus(STATUS_EXECUTING,STATUS_PENDING)){
            Debug.E("Fail execute runner while already executing.");
            return false;
        }
        if (taskId==null||taskId.length()<=0){
            runner.setTaskId(task.getClass().getName()+"_"+task.hashCode()+"_"+ option+"_"+hashCode()+ "_"+System.currentTimeMillis()+(Math.random()*10000000));
        }
        if (!mQueue.contains(runner)&&mQueue.add(runner)){
            runner.setStatus(STATUS_ADD,true);
        }
        if (!Option.isOptionEnabled(option,Option.EXECUTE)){//Just return while not need execute
            Debug.D("Not need execute while execute not enabled."+runner.getTask());
            return true;
        }
        runner.setStatus(STATUS_PENDING,true);
        executor.execute(runner);
        return true;
    }

    private boolean executeSavedTask(String taskId,byte[] bytes,boolean deleteWhileFail){
        if (null==taskId||taskId.length()<=0||null==bytes||bytes.length<=0){
            return false;
        }
        TaskRuntime taskRunner=findFirst((TaskRuntime data)->null!=data&&data.isTaskIdEquals(taskId));
        if (null!=taskRunner){
            return false;//Not need execute while already executing
        }
        Parcel parcel=Parcel.obtain();
        try {
            parcel.unmarshall(bytes,0,bytes.length);
            parcel.setDataPosition(0);
            String version=parcel.readString();
            int option=parcel.readInt();
            Parcelable parcelable=mParceler.readParcelable(parcel);
            parcel.recycle();
            parcel=null;
            Debug.D("读取到 "+parcelable);
            if (null!=parcelable&&parcelable instanceof Task){
                return execute(parcelable,option&(~Option.EXECUTE),taskId);
            }
        }catch (Exception e){
            Debug.E("Exception execute saved task.e="+e,e);
            e.printStackTrace();
        }finally {
            if (null!=parcel){
                parcel.recycle();
            }
        }
        if (deleteWhileFail){
            TaskSaver taskSaver=mTaskSaver;
            if (null!=taskSaver){
                Debug.D("To delete save task while execute load fail."+taskId);
                taskSaver.delete(taskId);
            }
        }
        return false;
    }

    private boolean saveTask(TaskRuntime runner){
        Task task=null!=runner?runner.getTask():null;
        if (null==task||!(task instanceof Parcelable)){
            return false;
        }
        String taskId=runner.getTaskId();
        TaskSaver taskSaver=mTaskSaver;
        if (null!=taskId&&taskId.length()>0&&null!=taskSaver){//To save task
            Debug.D("写入 "+task);
            Parcel parcel=Parcel.obtain();
            parcel.setDataPosition(0);
            parcel.writeString("version");
            parcel.writeInt(runner.getOption());
            mParceler.writeParcelable(parcel,task,0);
            byte[] bytes=parcel.marshall();
            boolean succeed=taskSaver.write(taskId,bytes);
            parcel.recycle();
            return succeed;
        }
        return false;
    }

    @Override
    public void findTask(OnTaskFind onTaskFind) {
        findAllTask((TaskRuntime data)-> null!=data&&onTaskFind.onTaskFind(data.getTask(),
        data.getStatus(),data.getOption())?null:false,Integer.MAX_VALUE);
    }

    private TaskRuntime findTaskData(Task task, boolean autoCreate){
        TaskRuntime taskData= null!=task?findFirst((TaskRuntime data)-> null!=data&&data.isTaskEquals(task)):null;
        return null!=taskData?taskData:(autoCreate?new TaskRuntime(task,this){
            @Override
            protected void onStatusChanged(int last, int current) {
                mExecutorFull=current==STATUS_FINISH?false:mExecutorFull;
                //任务开始和结束的时候需要检查是否需要保存
                if ((current==STATUS_FINISH||current==STATUS_PENDING)&&!isNeedDelete()){
                    saveTask(this);
                }
                notifyStatusChange(this,mListeners);
                //
                if (!mExecutorFull&&post(()->{
                    TaskRuntime nextRunner=findFirst((TaskRuntime data)->null!=data&&data.isAnyStatus(STATUS_WAITING));
                    if (!mExecutorFull&&null!=nextRunner){
                        execute(nextRunner);
                    }
                },0)){
                    //Do nothing
                }
            }
        }:null);
    }

    private TaskRuntime findFirst(Matcher<TaskRuntime> matcher){
        List<TaskRuntime> runners=findAllTask(matcher,1);
        return null!=runners&&runners.size()>0?runners.get(0):null;
    }

    private List<TaskRuntime> findAllTask(Matcher<TaskRuntime> matcher,int max){
        List<TaskRuntime> queue=null!=matcher?mQueue:null;
        List<TaskRuntime> result=null;
        if(null!=queue){
            result=new ArrayList<>(Math.min(10,max<=0?0:max));
            for (TaskRuntime runner:queue) {
                if (result.size()>=max){
                    break;
                }
                Boolean match=matcher.match(runner);
                if (null==match){
                    break;
                }else if (match&&null!=runner){
                    result.add(runner);
                }
            }
        }
        return result;
    }

    private void notifyStatusChange(TaskRuntime runner,Map<Listener,Matcher<Task>> listeners){
        Set<Listener> set=null!=listeners&&null!=runner?listeners.keySet():null;
        if (null!=set){
            for (Listener listener:set) {
                if (null!=listener&&listener instanceof OnStatusChangeListener){
                    notifyStatusChange(runner,listeners.get(listener),(OnStatusChangeListener)listener);
                }
            }
        }
    }

    private void notifyStatusChange(TaskRuntime runner,Matcher<Task> matcher ,Listener listener){
        Boolean matched=null!=matcher&&null!=listener?matcher.match(runner.getTask()):null;
        if (null!=matched&&matched){
            notifyStatusChange(runner,listener);
        }
    }

    private void notifyStatusChange(TaskRuntime runner,Listener listener){
        if (null==listener||null==runner){
            return;
        }else if (listener instanceof UiListener&&!Utils.isUiThread()){
            post(()->notifyStatusChange(runner,listener),-1);
            return;
        }else if (listener instanceof OnStatusChangeListener){
            ((OnStatusChangeListener)listener).onStatusChanged(runner.getStatus(),runner.getTask(), TaskExecutor.this);
        }
    }

    public final boolean post(Runnable runnable, int delay){
        return null!=runnable&mHandler.postDelayed(runnable,delay<=0?0:delay);
    }
}
