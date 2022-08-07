package com.luckmerlin.browser;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.Result;
import com.luckmerlin.core.Section;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskExecutor;
import com.luckmerlin.task.TaskGroup;

public class ConveyorService extends Service {
    private final ConveyorBinder mConveyorBinder=new ConveyorBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.D("EEEE onCreate "+this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mConveyorBinder;
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

    private static class ConveyorBinder extends Binder implements TaskExecutor<Task> {
        private final TaskGroup mTasks=new TaskGroup();

        @Override
        public boolean add(Task task) {
            return mTasks.add(task);
        }

        @Override
        public boolean remove(Object task) {
            return mTasks.remove(task);
        }

        @Override
        public Object find(Object task) {
            return mTasks.find(task);
        }

        @Override
        public Result<Section<Task>> load(Task from, Matcher<Task> matcher) {
            return mTasks.load(from,matcher);
        }

        @Override
        public Task getExecuting() {
            return mTasks.getExecuting();
        }
    }
}
