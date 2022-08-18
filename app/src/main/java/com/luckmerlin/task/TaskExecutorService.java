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
    private final ExecutorTaskSaver mExecutorTaskSaver=new ExecutorTaskSaver();
    private ExecutorBinder mExecutorBinder=new ExecutorBinder(new TaskExecutor(mExecutorTaskSaver));

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
        public  void match(Matcher<TaskExecutor.ExecuteTask> matcher) {
            mExecutor.match(matcher);
        }

        @Override
        public Executor setListener(Listener listener) {
            return mExecutor.setListener(listener);
        }
    }

    private static final class ExecutorTaskSaver implements TaskSaver{
        private static final String POSTFIX=".lmt";
        private final String mSaveFolder= "/sdcard/";

        @Override
        public void load(Matcher<Task> matcher) {
            String folder=mSaveFolder;
            if (null==matcher||null==folder||folder.length()<=0){
                return;
            }
            new File(folder).listFiles((File pathname)-> {
                Parcelable parcelable=readParcelableFile(pathname);
                if (null!=parcelable&&parcelable instanceof Task){
                    matcher.match((Task)parcelable);
                }
                return false;
            });
        }

        private Parcelable readParcelableFile(File pathname){
            if (null!=pathname&&pathname.isFile()&&pathname.getName().endsWith(POSTFIX)){
                FileInputStream inputStream=null;
                Parcel parcel=null;
                try {
                    inputStream=new FileInputStream(pathname);
                    int available=inputStream.available();
                    if (available>=Integer.MAX_VALUE){
                        return null;
                    }
                    byte[] buffer=new byte[available];
                    int read=inputStream.read(buffer);
                    if (read!=available){
                        return null;
                    }
                    parcel=Parcel.obtain();
                    parcel.unmarshall(buffer,0,available);
                    parcel.setDataPosition(0);
                    return parcel.readParcelable(getClass().getClassLoader());
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    Utils.closeStream(inputStream);
                    if (null!=parcel){
                        parcel.recycle();
                    }
                }
            }
            return null;
        }

        @Override
        public boolean save(Task task) {
            if (null==task||!(task instanceof Parcelable)){
                return false;
            }
            String folder=mSaveFolder;
            if (null==folder||folder.length()<=0){
                return false;
            }
            FileOutputStream outputStream=null;
            Parcel parcel=null;
            try {
                Debug.D("Saving tasK."+task);
                parcel=Parcel.obtain();
                parcel.setDataPosition(0);
                parcel.writeParcelable((Parcelable)task,0);
                byte[] taskBytes=parcel.marshall();
                if (null==taskBytes||taskBytes.length<=0){
                    Debug.W("Fail save tasK while write bytes invalid."+task);
                    return false;
                }
                int hasCode=System.identityHashCode(this);
                String id=hasCode+"_"+System.identityHashCode(task)+"_"+taskBytes.length+POSTFIX;
                File file=new File(folder,id);
                if (file.exists()){
                    return false;
                }
                file.createNewFile();
                if (!file.exists()){
                    return false;
                }
                outputStream=new FileOutputStream(file);
                outputStream.write(taskBytes);
                outputStream.flush();
                Debug.D("Saved tasK."+file+" "+task);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                Utils.closeStream(outputStream);
                if (null!=parcel){
                    parcel.recycle();
                }
            }
            return false;
        }
    }
}
