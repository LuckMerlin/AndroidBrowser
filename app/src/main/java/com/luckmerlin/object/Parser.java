package com.luckmerlin.object;

import org.json.JSONException;

public interface Parser<F,T> {
    T onParse(F from) throws JSONException;
}
