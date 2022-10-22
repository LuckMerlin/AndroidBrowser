package com.luckmerlin.dialog;

import com.luckmerlin.view.Content;

public interface Window {
    boolean setContentView(Content content);
    boolean dismiss();
    boolean isShowing();
}
