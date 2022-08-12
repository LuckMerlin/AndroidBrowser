package com.luckmerlin.task;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

public class TaskExecutorService extends Service {
    private ExecutorBinder mExecutorBinder=new ExecutorBinder(new TaskExecutor());

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
        public boolean execute(Task task, OnProgressChange callback) {
            return mExecutor.execute(task,callback);
        }

        @Override
        public List<Task> getExecuting() {
            return mExecutor.getExecuting();
        }
    }
}
