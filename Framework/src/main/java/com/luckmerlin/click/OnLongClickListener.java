package com.luckmerlin.click;

import android.view.View;

public interface OnLongClickListener extends Listener{
    boolean onLongClick(View view, int clickId, Object obj);
}
