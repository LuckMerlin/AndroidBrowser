package com.luckmerlin.browser;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.CodeResult;
import com.luckmerlin.core.Section;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskExecutor;
import com.luckmerlin.task.TaskExecutorService;
import com.luckmerlin.task.TaskGroup;
import com.luckmerlin.task.TaskSaver;

import java.util.Map;
import java.util.Set;

public class ConveyorService extends TaskExecutorService {
    @Override
    public void onCreate() {
        super.onCreate();
        Debug.D("EEEE onCreate "+this);
    }

    @Override
    protected TaskSaver onCreateTaskSaver() {
        return new ConveyorTaskSaver(getApplication());
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

}
