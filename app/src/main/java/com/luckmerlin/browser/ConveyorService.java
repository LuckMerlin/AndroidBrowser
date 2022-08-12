package com.luckmerlin.browser;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.CodeResult;
import com.luckmerlin.core.Section;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskExecutor;
import com.luckmerlin.task.TaskExecutorService;
import com.luckmerlin.task.TaskGroup;

public class ConveyorService extends TaskExecutorService {
    @Override
    public void onCreate() {
        super.onCreate();
        Debug.D("EEEE onCreate "+this);
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

}
