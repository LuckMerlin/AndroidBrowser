package com.luckmerlin.browser.task;

import android.content.Context;
import android.view.View;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.MessageResult;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class TestTask extends AbstractTask {

    public TestTask() {
        super(null);
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        Debug.D("DDDDDDD "+this);
        Progress progress=new Progress().setTitle("Title进度").setSpeed("333.44MB/S").setPosition(0).setTotal(100);
        File fromFile=new File().setName("FromName").setLength(100000);
        File toFile=new File().setName("ToName").setLength(100000);
        final boolean[] canceled=new boolean[]{false};
        Doing doing=new Doing().setDoingBinding(new DialogButtonBinding(ViewBinding.create(R.string.cancel).
                setListener((OnClickListener)(View view, int clickId, int count, Object obj)-> {
                    canceled[0]=true;
                    return true;
                })));
        while (progress.getPosition()<10){
            progress.setDoing(doing.setFrom(fromFile).setTo(toFile).setProgress(progress.intValue()));
            notifyProgress(progress);
            try {
                if (canceled[0]){
//                    return (MessageResult)(Context context)-> (null!=context?context.getString(R.string.cancel):"Cancel")+".";
                }
                progress.setPosition(progress.getPosition()+1);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
