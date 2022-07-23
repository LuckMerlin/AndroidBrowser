package com.luckmerlin.object;

public interface Parser<F,T> {
    T onParse(F from);
}
