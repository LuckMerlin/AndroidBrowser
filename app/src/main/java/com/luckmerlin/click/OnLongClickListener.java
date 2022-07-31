package com.luckmerlin.click;

import android.view.View;

import com.luckmerlin.browser.binding.Iterate;

public interface OnLongClickListener extends Iterate {
    boolean onLongClick(View view, int clickId, Object obj);
}
