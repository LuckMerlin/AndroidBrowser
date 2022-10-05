package com.luckmerlin.task;

public final class  Option {
    public final static int ENABLE=0b01;
    public final static int CANCEL=0b10|ENABLE;
    public final static int DELETE=0b100|ENABLE;
    public final static int RESET=0b1000|ENABLE;
    public final static int PENDING=0b10000|ENABLE;
    public final static int EXECUTE=0b100000|PENDING;
    public final static int DELETE_SUCCEED=0b1000000|ENABLE;

    private Option(){

    }

    protected static boolean isOptionEnabled(int src,int option){
        return (option&src)==(option|ENABLE);
    }

    protected static int enableOption(int src,int option,boolean enable){
        return enable?(src&option)|ENABLE:(src&option)&~ENABLE;
    }
}
