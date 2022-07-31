package com.luckmerlin.click;

import android.view.View;

import com.luckmerlin.browser.binding.Iterate;

public interface OnClickListener extends Iterate {
    boolean onClick(View view, int clickId,int count, Object obj);
}
