package com.luckmerlin.browser;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;

import com.luckmerlin.browser.client.Client;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.client.NasClient;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.data.Preferences;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnTaskFind;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConveyorService extends Service {
    private ExecutorBinder mExecutorBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.D("EEEE onCreate "+this);
//        SharedPreferences preferences=getSharedPreferences("tasks",Context.MODE_PRIVATE);
        BrowserTaskExecutor executor=new BrowserTaskExecutor(getApplication());
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
        private Preferences mClientPreferences;

        public BrowserTaskExecutor(Context context){
            super(context,new Preferences(context.getSharedPreferences("tasks",Context.MODE_PRIVATE)));
            Preferences preferences=mClientPreferences=new Preferences(context.getSharedPreferences("clients",MODE_PRIVATE));
            List<Client> clients=new ArrayList<>();
            preferences.load((String key, byte[] bytes)-> {
                if (null==key||key.length()<=0||null==bytes||bytes.length<=0){
                    return;
                }
                Parcel parcel=Parcel.obtain();
                parcel.unmarshall(bytes,0,bytes.length);
                parcel.setDataPosition(0);
                Object obj=readParcelable(parcel);
                parcel.recycle();
                if (null!=obj&&obj instanceof Client){
                    clients.add((Client)obj);
                }
            });
            if (clients.size()<=0){
                NasClient client=new NasClient("http://192.168.0.2:6666");
                client.setName("NAS");
                clients.add(client);
                client=new NasClient("http://192.168.0.6:89");
                client.setName("DEV");
                clients.add(client);
                LocalClient localClient=new LocalClient();
                localClient.setName(context.getString(R.string.local));
                clients.add(localClient);
            }
            mClients=clients;
        }

        @Override
        public boolean saveClient(Client client, boolean delete) {
            ClientMeta meta=null!=client?client.getMeta():null;
            String host=null!=meta?meta.getHost():null;
            Preferences clientPreferences=mClientPreferences;
            if (null==host||host.length()<=0||null==clientPreferences){
                return false;
            }else if (delete){
                return clientPreferences.delete(host);
            }
            Parcel parcel=Parcel.obtain();
            parcel.setDataPosition(0);
            writeParcelable(parcel,client,0);
            byte[] bytes=parcel.marshall();
            parcel.recycle();
            return clientPreferences.write(host,bytes);
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

        @Override
        public boolean saveClient(Client client, boolean delete) {
            return mExecutor.saveClient(client,delete);
        }
    }
}
