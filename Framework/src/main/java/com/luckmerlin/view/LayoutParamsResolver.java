package com.luckmerlin.view;

import android.content.Context;
import android.view.ViewGroup;

public interface LayoutParamsResolver {
    void onResolveLayoutParams(Context context,ViewGroup.LayoutParams params);
}
