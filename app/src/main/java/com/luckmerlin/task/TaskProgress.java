package com.luckmerlin.task;

public final class TaskProgress implements Progress{
    private long mTotal;
    private long mPosition;
    private String mTitle;
    private String mSpeed;

    public TaskProgress setPosition(long mPosition) {
        this.mPosition = mPosition;
        return this;
    }

    public TaskProgress setSpeed(String mSpeed) {
        this.mSpeed = mSpeed;
        return this;
    }

    public TaskProgress setTitle(String mTitle) {
        this.mTitle = mTitle;
        return this;
    }

    public TaskProgress setTotal(long mTotal) {
        this.mTotal = mTotal;
        return this;
    }

    @Override
    public long getTotal() {
        return mTotal;
    }

    @Override
    public long getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSpeed() {
        return mSpeed;
    }
}
