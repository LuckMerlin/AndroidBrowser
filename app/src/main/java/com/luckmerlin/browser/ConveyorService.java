package com.luckmerlin.browser;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;

import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.client.NasClient;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskExecutor;
import com.luckmerlin.task.TaskSaver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConveyorService extends Service {
    private ExecutorBinder mExecutorBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.D("EEEE onCreate "+this);
        List<Client> clients=new ArrayList<>();
        clients.add(new LocalClient());
        clients.add(new NasClient("http://192.168.0.10:6666","DEV"));
//        clients.add(new NasClient("http://192.168.0.2:6666","NAS"));
        ConveyorTaskSaver taskSaver=new ConveyorTaskSaver(getApplication());
//        taskSaver=null;/
        BrowserTaskExecutor executor=new BrowserTaskExecutor(getApplication(),taskSaver,clients);
        mExecutorBinder=new ExecutorBinder(executor);
//        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
        //git remote set-url origin https://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@github.com/LuckMerlin/TsServer.git
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mExecutorBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Debug.D("EEEE onDestroy "+this);
    }

    private static class ConveyorTaskSaver implements com.luckmerlin.task.TaskSaver{
        private final SharedPreferences mPreferences;
        private final static String POSTFIX="LMTask";

        protected ConveyorTaskSaver(Application application){
            mPreferences=null!=application?application.getSharedPreferences("TaskSaver", Context.MODE_PRIVATE):null;
        }

        @Override
        public void load(TaskBytesReader bytesReader) {
            SharedPreferences preferences=mPreferences;
            if (null==bytesReader||null==preferences){
                return;
            }
            Map<String,?> objectMap=preferences.getAll();
            Set<String> set=null!=objectMap?objectMap.keySet():null;
            if (null==set){
                return;
            }
            Object childObj=null;
            SharedPreferences.Editor editor=preferences.edit();
            for (String taskId:set) {
                if (null==taskId||!taskId.endsWith(POSTFIX)){
                    continue;
                }
                childObj=objectMap.get(taskId);
                byte[] bytes=null!=childObj&&childObj instanceof String? Base64.decode((String)childObj,Base64.URL_SAFE):null;
                editor.remove(taskId);
                Task task=null; String newId=null;
                if (null==bytes||bytes.length<=0||null==(task=bytesReader.readTaskBytes(bytes))||
                        null==(newId=generateTaskId(task))||newId.length()<=0){
                    Debug.D("Delete saved task while bytes invalid.");
                }else{
                    editor.putString(newId,(String)childObj);
                }
                editor.commit();
            }
        }

        @Override
        public boolean delete(Task task) {
            SharedPreferences preferences=null!=task?mPreferences:null;
            if (null==preferences){
                return false;
            }
            Debug.D("Deleting save tasK."+task);
            String id=generateTaskId(task);
            return null!=id&&preferences.edit().remove(id).commit();
        }

        @Override
        public boolean write(Task task, byte[] taskBytes) {
            SharedPreferences preferences=mPreferences;
            if (null==task||null==taskBytes||taskBytes.length<=0||null==preferences){
                return false;
            }
            Debug.D("Saving tasK."+task);
            String id=generateTaskId(task);
            if (null==id||id.length()<=0){
                Debug.W("Fail save tasK while generate task id invalid."+task);
                return false;
            }
            return preferences.edit().putString(id,Base64.encodeToString(taskBytes, Base64.URL_SAFE)).commit();
        }

        private String generateTaskId(Task task){
            if (null==task){
                return null;
            }
            int hasCode=System.identityHashCode(this);
            return hasCode+"_"+System.identityHashCode(task)+POSTFIX;
        }
    }

    private static class BrowserTaskExecutor extends TaskExecutor implements BrowserExecutor{
        private List<Client> mClients;

        public BrowserTaskExecutor(Context context,TaskSaver taskSaver,List<Client> clients){
            super(context,taskSaver);
            mClients=clients;
        }

        @Override
        public boolean client(Matcher<Client> matcher) {
            return match(mClients,matcher);
        }
    }

    public static class ExecutorBinder extends Binder implements BrowserExecutor {
        protected final BrowserTaskExecutor mExecutor;

        public ExecutorBinder(BrowserTaskExecutor executor){
            mExecutor=executor;
        }

        @Override
        public boolean client(Matcher<Client> matcher) {
            BrowserTaskExecutor executor=mExecutor;
            return null!=executor&&executor.client(matcher);
        }

        @Override
        public boolean execute(Object task,int option, OnProgressChange callback) {
            return mExecutor.execute(task,option,callback);
        }

        @Override
        public boolean option(Object task, int option) {
            return mExecutor.option(task,option);
        }

        @Override
        public  void match(Matcher<TaskExecutor.ExecuteTask> matcher) {
            mExecutor.match(matcher);
        }

        @Override
        public Executor putListener(Listener listener, Matcher<Task> matcher, boolean notify) {
            return mExecutor.putListener(listener,matcher,notify);
        }

        @Override
        public Executor removeListener(Listener listener) {
            return mExecutor.removeListener(listener);
        }
    }
}
