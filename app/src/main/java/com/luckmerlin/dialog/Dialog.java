package com.luckmerlin.dialog;

import com.luckmerlin.view.Content;
import com.luckmerlin.view.LayoutParamsResolver;

public interface Dialog {
    boolean setContentView(Content content);
    boolean show(LayoutParamsResolver resolver);
    boolean dismiss();
    boolean isShowing();
}
