package com.luckmerlin.core;

public interface Group<T>{
    boolean add(T task);
    boolean remove(Object task);
    Object find(Object task);
    Result<Section<T>> load(T from, Matcher<T> matcher);
}
