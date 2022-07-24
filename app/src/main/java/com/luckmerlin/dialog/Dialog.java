package com.luckmerlin.dialog;

import android.view.View;
import android.view.ViewGroup;

public interface Dialog {
    boolean setContentView(View view, ViewGroup.LayoutParams params);
    boolean show(ViewGroup.LayoutParams params);
    boolean dismiss();
    boolean isShowing();
}
