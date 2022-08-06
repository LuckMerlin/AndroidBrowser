package com.luckmerlin.object;

public interface Converter<F,T> {
    T onConvert(F from);
}
