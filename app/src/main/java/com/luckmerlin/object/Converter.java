package com.luckmerlin.object;

@Deprecated
public interface Converter<F,T> {
    T onConvert(F from);
}
