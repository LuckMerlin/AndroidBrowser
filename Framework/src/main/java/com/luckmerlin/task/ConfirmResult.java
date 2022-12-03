package com.luckmerlin.task;

import android.content.Context;

import com.luckmerlin.core.Result;

public interface ConfirmResult extends Result {
    Confirm1 makeConfirm(Context context);
}
