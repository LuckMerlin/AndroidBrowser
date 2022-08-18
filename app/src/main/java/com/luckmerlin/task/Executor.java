package com.luckmerlin.task;

import com.luckmerlin.core.Matcher;

public interface Executor {

    interface Listener{

    }

    public interface OnAddRemoveChangeListener extends Listener{
        void onAddRemoveChanged(int status,Task task,Executor executor);
    }

    public final static int STATUS_PENDING=2000;
    public final static int STATUS_EXECUTING=2001;
    public final static int STATUS_WAITING=2002;
    public final static int STATUS_FINISH=2003;
    public final static int STATUS_INTERRUPTED=2004;
    public final static int STATUS_START_LOAD_SAVED=2005;
    public final static int STATUS_FINISH_LOAD_SAVED=2006;
    public final static int STATUS_ADD=2007;
    public final static int STATUS_REMOVE=2008;

    boolean execute(Task task,OnProgressChange callback);
    void match(Matcher<TaskExecutor.ExecuteTask> matcher);
    Executor setListener(Listener listener);
}
