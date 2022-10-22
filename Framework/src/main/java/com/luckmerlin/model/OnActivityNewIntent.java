package com.luckmerlin.model;

import android.app.Activity;
import android.content.Intent;

public interface OnActivityNewIntent {
    void onNewIntent(Activity activity, Intent intent);
}
