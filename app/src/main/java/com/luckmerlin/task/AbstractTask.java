package com.luckmerlin.task;

public abstract class AbstractTask<A,R> implements Task<A,R>{
    private String mName;

    public final AbstractTask<A,R> setName(String name) {
        this.mName = name;
        return this;
    }

    @Override
    public final String getName() {
        return mName;
    }

}
