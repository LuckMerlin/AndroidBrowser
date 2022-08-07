package com.merlin.adapter;

public class Query<T> {
    private String mName;
    private T mFrom;

    public final Query<T> setFrom(T from) {
        this.mFrom = from;
        return this;
    }

    public final Query<T> setName(String name) {
        this.mName = name;
        return this;
    }

    public final String getName() {
        return mName;
    }

    public final T getFrom() {
        return mFrom;
    }
}
