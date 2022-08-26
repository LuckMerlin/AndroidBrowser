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
import com.luckmerlin.core.Matcher;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TaskExecutorService extends Service {
    private ExecutorBinder mExecutorBinder;

    protected TaskSaver onCreateTaskSaver(){
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutorBinder=new ExecutorBinder(new TaskExecutor(onCreateTaskSaver()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mExecutorBinder;
    }

    public static class ExecutorBinder extends Binder implements Executor{
        protected final Executor mExecutor;

        public ExecutorBinder(Executor executor){
            mExecutor=null!=executor?executor:new TaskExecutor();
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
        public Executor putListener(Listener listener, Matcher<Task> matcher) {
            return mExecutor.putListener(listener,matcher);
        }

        @Override
        public Executor removeListener(Listener listener) {
            return mExecutor.removeListener(listener);
        }
    }
}
