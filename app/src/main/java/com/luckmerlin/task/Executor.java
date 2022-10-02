package com.luckmerlin.task;

import com.luckmerlin.core.Matcher;

public interface Executor {

    public static interface Option{
        public final static int NONE=0;
        public final static int CANCEL=1;
        public final static int DELETE=2;
        public final static int DELETE_SUCCEED=32|DELETE&~CANCEL;
    }

    interface Listener{

    }

    public interface OnStatusChangeListener extends Listener{
        void onStatusChanged(int status,Task task,Executor executor);
    }

    public final static int STATUS_PENDING=2000;
    public final static int STATUS_EXECUTING=2001;
    public final static int STATUS_WAITING=2002;
    public final static int STATUS_FINISH=2003;
    public final static int STATUS_INTERRUPTED=2004;
    public final static int STATUS_START_LOAD_SAVED=2005;
    public final static int STATUS_FINISH_LOAD_SAVED=2006;
    public final static int STATUS_ADD=2007;
    public final static int STATUS_DELETE=2008;

    boolean execute(Object task,int option,OnProgressChange callback);
    boolean option(Object task,int option);
    void match(Matcher<TaskExecutor.ExecuteTask> matcher);
    Executor putListener(Listener listener,Matcher<Task> matcher,boolean notify);
    Executor removeListener(Listener listener);
}
