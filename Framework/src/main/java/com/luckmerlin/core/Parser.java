package com.luckmerlin.core;

public interface Parser<F,T> {
    T onParse(F from);
}
