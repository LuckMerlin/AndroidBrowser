package com.luckmerlin.task;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import com.luckmerlin.browser.BrowserExecutor;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.MatcherInvoker;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TaskExecutorService extends Service {
    private ExecutorBinder mExecutorBinder;

    protected TaskSaver onCreateTaskSaver(){
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        List<Client> clients=new ArrayList<>();
        clients.add(new LocalClient());
        BrowserTaskExecutor executor=new BrowserTaskExecutor(onCreateTaskSaver(),clients);
        mExecutorBinder=new ExecutorBinder(executor);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mExecutorBinder;
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
        public Executor putListener(Listener listener, Matcher<Task> matcher,boolean notify) {
            return mExecutor.putListener(listener,matcher,notify);
        }

        @Override
        public Executor removeListener(Listener listener) {
            return mExecutor.removeListener(listener);
        }
    }

    private static class BrowserTaskExecutor extends TaskExecutor implements BrowserExecutor{
        private List<Client> mClients;

        public BrowserTaskExecutor(TaskSaver taskSaver,List<Client> clients){
            super(taskSaver);
            mClients=clients;
        }

        @Override
        public boolean client(Matcher<Client> matcher) {
            return match(mClients,matcher);
        }
    }
}
