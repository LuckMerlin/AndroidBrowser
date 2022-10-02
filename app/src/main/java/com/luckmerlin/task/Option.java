package com.luckmerlin.task;

public interface Option {
    public final static int NONE=0;
    public final static int CANCEL=1;
    public final static int DELETE=2;
    public final static int DELETE_SUCCEED=32|DELETE&~CANCEL;
}
