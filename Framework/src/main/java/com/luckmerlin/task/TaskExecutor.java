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
    private final List<TaskRunner> mQueue=new CopyOnWriteArrayList<>();
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
            if (null!=r&&r instanceof TaskRunner){
               ((TaskRunner)r).setStatus(STATUS_WAITING,true);
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
        TaskRunner taskRunner=findTaskData((Task)taskObj,true);
        return execute(null!=taskRunner?taskRunner.setOption(option).setTaskId(taskId):null);
    }

    private boolean execute(TaskRunner runner){
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
            taskSaver.delete(taskId);//Try to delete saved task
        }
        if (!Option.isOptionEnabled(option,Option.EXECUTE)){//Just return while not need execute
            Debug.D("Not need execute while execute not enabled."+runner.getTask());
            return true;
        }
        if (runner.isAnyStatus(STATUS_EXECUTING,STATUS_PENDING)){
            Debug.E("Fail execute runner while already executing.");
            return false;
        }
        if (taskId==null||taskId.length()<=0){
            mQueue.add(runner.setTaskId(task.getClass().getName()+"_"+task.hashCode()+"_"+
                    option+"_"+hashCode()+ "_"+System.currentTimeMillis()+(Math.random()*10000000)));
            runner.setStatus(STATUS_ADD,true);
        }
        runner.setStatus(STATUS_PENDING,true);
        executor.execute(runner);
        return true;
    }

    private boolean executeSavedTask(String taskId,byte[] bytes,boolean deleteWhileFail){
        if (null==taskId||taskId.length()<=0||null==bytes||bytes.length<=0){
            return false;
        }
        TaskRunner taskRunner=findFirst((TaskRunner data)->null!=data&&data.isTaskIdEquals(taskId));
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

    private boolean saveTask(TaskRunner runner){
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
        findAllTask((TaskRunner data)-> null!=data&&onTaskFind.onTaskFind(data.getTask(),
        data.getStatus(),data.getOption())?null:false,Integer.MAX_VALUE);
    }

    private TaskRunner findTaskData(Task task, boolean autoCreate){
        TaskRunner taskData= null!=task?findFirst((TaskRunner data)-> null!=data&&data.isTaskEquals(task)):null;
        return null!=taskData?taskData:(autoCreate?new TaskRunner(){
            @Override
            protected void onStatusChanged(int last, int current) {
                mExecutorFull=current==STATUS_FINISH?false:mExecutorFull;
                //任务开始和结束的时候需要检查是否需要保存
                if ((current==STATUS_FINISH||current==STATUS_PENDING)&&!isNeedDelete()){
                    saveTask(this);
                }
                notifyStatusChange(current,this,mListeners);
                //
                if (!mExecutorFull&&post(()->{
                    TaskRunner nextRunner=findFirst((TaskRunner data)->null!=data&&data.isAnyStatus(STATUS_WAITING));
                    if (!mExecutorFull&&null!=nextRunner){
                        execute(nextRunner);
                    }
                },0)){
                    //Do nothing
                }
            }

            @Override
            protected Task getTask() {
                return task;
            }
        }:null);
    }

    private TaskRunner findFirst(Matcher<TaskRunner> matcher){
        List<TaskRunner> runners=findAllTask(matcher,1);
        return null!=runners&&runners.size()>0?runners.get(0):null;
    }

    private List<TaskRunner> findAllTask(Matcher<TaskRunner> matcher,int max){
        List<TaskRunner> queue=null!=matcher?mQueue:null;
        List<TaskRunner> result=null;
        if(null!=queue){
            result=new ArrayList<>(Math.min(10,max<=0?0:max));
            for (TaskRunner runner:queue) {
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

    private void notifyStatusChange(int status,TaskRunner runner,Map<Listener,Matcher<Task>> listeners){
        Set<Listener> set=null!=listeners&&null!=runner?listeners.keySet():null;
        if (null!=set){
            Matcher<Task> matcher=null;Boolean matched=null;
            for (Listener listener:set) {
                if (null!=listener&&listener instanceof OnStatusChangeListener&&null!=
                        (matcher=listeners.get(listener))&&null!= (matched=matcher.match(runner.getTask()))&&matched){
                    notifyStatusChange(status,runner,(OnStatusChangeListener)listener);
                }
            }
        }
    }

    private void notifyStatusChange(int status,TaskRunner runner,OnStatusChangeListener listener){
        if (null==listener||null==runner){
            return;
        }else if (listener instanceof UiListener&&!Utils.isUiThread()){
            post(()->notifyStatusChange(status,runner,listener),-1);
            return;
        }
        listener.onStatusChanged(status,runner.getTask(), TaskExecutor.this);
    }

    public final boolean post(Runnable runnable, int delay){
        return null!=runnable&mHandler.postDelayed(runnable,delay<=0?0:delay);
    }
}
