package com.luckmerlin.task;

public class FromTo<F,T>{
    private F mFrom;
    private T mTo;

    public FromTo<F,T> setFrom(F from) {
        this.mFrom = from;
        return this;
    }

    public FromTo<F,T> setTo(T to) {
        this.mTo = to;
        return this;
    }

    public F getFrom() {
        return mFrom;
    }

    public T getTo() {
        return mTo;
    }
}
