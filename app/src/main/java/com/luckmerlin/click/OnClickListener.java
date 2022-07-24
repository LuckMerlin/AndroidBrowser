package com.luckmerlin.click;

import android.view.View;

public interface OnClickListener {
    boolean onClick(View view, int clickId,int count, Object obj);
}
