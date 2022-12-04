package com.luckmerlin.browser.dialog;

import com.luckmerlin.task.Ongoing;
import com.luckmerlin.task.Task;

public class AutoDismissSucceedTask implements DoingTaskContent.AutoDismiss {
    @Override
    public int onResolveAutoDismiss(Task task) {
        Ongoing ongoing=null!=task?task.getOngoing():null;
        return null!=ongoing&&ongoing.isSucceed()?1000:-1;
    }
}
