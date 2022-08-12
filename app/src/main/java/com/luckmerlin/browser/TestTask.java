package com.luckmerlin.browser;

import android.app.Activity;

import com.luckmerlin.core.CodeResult;
import com.luckmerlin.task.AbstractTask;

public class TestTask extends AbstractTask {
    Activity activity;

    public TestTask(Activity activity){
        super(null);
        this.activity=activity;
    }

    @Override
    protected CodeResult onExecute(Object arg) {
        return null;
    }

    //    @Override
//    protected Object onExecute(Object arg) {
//        while (null!=activity &&!((Activity)activity).isDestroyed()){
//            try {
//                Progress progress=getProgress();
//                int[] ddd=new int[1];
//                final Random random=new Random();
//                progress=null!=progress?progress:new Progress() {
//                    @Override
//                    public long getTotal() {
//                        return 100;
//                    }
//
//                    @Override
//                    public long getPosition() {
//                        return ++ddd[0]>100?ddd[0]=0:ddd[0];
//                    }
//
//                    @Override
//                    public String getSpeed() {
//                        return random.nextInt(10000)+"MB";
//                    }
//
//                    @Override
//                    public String getTitle() {
//                        return random.nextInt(24234242)+"发达";
//                    }
//                };
//                notifyProgress(progress);
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
}
