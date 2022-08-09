package com.luckmerlin.browser;

import android.app.Activity;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;

public class TestTask extends AbstractTask {
    Activity activity;

    public TestTask(Activity activity){
        super(null);
        this.activity=activity;
    }

    @Override
    protected Object onExecute(Object arg) {
        while (null!=activity &&!((Activity)activity).isDestroyed()){
            try {
                Progress progress=getProgress();
                int[] ddd=new int[1];
                progress=null!=progress?progress:new Progress() {
                    @Override
                    public long getTotal() {
                        return 100;
                    }

                    @Override
                    public long getPosition() {
                        return ++ddd[0]>100?ddd[0]=0:ddd[0];
                    }
                };
                notifyProgress(progress);
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
