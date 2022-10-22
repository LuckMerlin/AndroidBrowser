package com.luckmerlin.core;

public interface Group<T>{
    boolean add(T task);
    boolean remove(Object task);
    Object find(Object task);
//    CodeResult<Section<T>> load(T from, Matcher<T,R> matcher);
}
