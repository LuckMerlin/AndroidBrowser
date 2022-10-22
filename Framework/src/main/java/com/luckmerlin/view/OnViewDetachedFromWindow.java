package com.luckmerlin.view;

import android.view.View;

public interface OnViewDetachedFromWindow extends ViewAttachedListener{
    void onViewDetachedFromWindow(View v);
}
