package com.luckmerlin.browser;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;

import com.luckmerlin.browser.client.Client;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnTaskFind;
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
//        clients.add(new NasClient("http://192.168.0.2:6666","NAS"));
//        clients.add(new NasClient("http://192.168.0.6:89","Dev"));
        clients.add(new LocalClient());
//        clients.add(new NasClient("http://192.168.0.10:6666","DEV"));
        ConveyorTaskSaver taskSaver=new ConveyorTaskSaver(getApplication());
        BrowserTaskExecutor executor=new BrowserTaskExecutor(getApplication(),taskSaver,clients);
        mExecutorBinder=new ExecutorBinder(executor);
//        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
        //git remote set-url origin https://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@github.com/LuckMerlin/TsServer.git
    //
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

    private static class BrowserTaskExecutor extends TaskExecutor implements BrowserExecutor{
        private List<Client> mClients;

        public BrowserTaskExecutor(Context context,TaskSaver taskSaver,List<Client> clients){
            super(context,taskSaver);
            mClients=clients;
        }

        @Override
        public boolean client(Matcher<Client> matcher) {
            List<Client> clients=mClients;
            if (null!=clients&&null!=matcher){
                for (Client child:clients) {
                    matcher.match(child);
                }
            }
            return true;
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
        public boolean execute(Object task,int option) {
            return mExecutor.execute(task,option);
        }

        @Override
        public void findTask(OnTaskFind onTaskFind) {
            mExecutor.findTask(onTaskFind);
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

    private static class ConveyorTaskSaver implements TaskSaver{
        private final SharedPreferences mPreferences;

        protected ConveyorTaskSaver(Application application){
            mPreferences=null!=application?application.getSharedPreferences("tasks",Context.MODE_PRIVATE):null;
        }

        @Override
        public boolean delete(Object obj) {
            SharedPreferences preferences=mPreferences;
            if (null==obj||null==preferences){
                return false;
            }else if (obj instanceof String){
                return preferences.edit().remove((String)obj).commit();
            }
            return false;
        }

        @Override
        public void load(OnTaskLoad onTaskLoad) {
            SharedPreferences preferences=mPreferences;
            Map<String,?> map=null!=onTaskLoad&&null!=preferences?preferences.getAll():null;
            Set<String> set=null!=map?map.keySet():null;
            if (null!=set){
                Object value=null;byte[] bytes=null;
                for (String child:set) {
                    if (null!=(value=null!=child?map.get(child):null)&& value instanceof String&&
                            null!=(bytes=Base64.decode((String)value, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING))){
                        onTaskLoad.onTaskLoaded(child,bytes);
                    }
                }
            }
        }

        @Override
        public boolean write(String taskId, byte[] taskBytes) {
            SharedPreferences preferences=mPreferences;
            if (null==taskId||taskId.length()<=0||null==preferences||null==taskBytes||taskBytes.length<=0){
                return false;
            }
            return preferences.edit().putString(taskId,
                    Base64.encodeToString(taskBytes,  Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING)).commit();
        }
    }
}
