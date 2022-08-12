package com.luckmerlin.browser.task;

import android.content.Context;
import com.luckmerlin.core.Result;

public interface ConfirmResult extends Result {
    String makeConfirmMessage(Context context);
}
