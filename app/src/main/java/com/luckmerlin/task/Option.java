package com.luckmerlin.task;

public final class  Option {
//    public final static int ENABLE=0b01;
    public final static int CANCEL=0b10;
    public final static int NOT_SAVE=0b100;
    @Deprecated
    public final static int DELETE=NOT_SAVE;
    public final static int RESET=0b1000;
    public final static int PENDING=0b10000;
    public final static int BACKGROUND=0b100000;
    public final static int EXECUTE=0b1000000;
    public final static int LAUNCH=EXECUTE|PENDING;
    public final static int LAUNCH_NOT_SAVE=LAUNCH|NOT_SAVE;
    public final static int DELETE_SUCCEED=0b10000000;

    private Option(){

    }

    protected static boolean isOptionEnabled(int src,int option){
        return (option&src)>0;
    }

    protected static int enableOption(int src,int option,boolean enable){
        return enable?src|option:src&(~option);
    }
}
