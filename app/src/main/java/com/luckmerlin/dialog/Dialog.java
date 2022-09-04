package com.luckmerlin.dialog;

import com.luckmerlin.view.Content;
import com.luckmerlin.view.LayoutParamsResolver;

public interface Dialog extends Window{
    boolean show(LayoutParamsResolver resolver);
}
