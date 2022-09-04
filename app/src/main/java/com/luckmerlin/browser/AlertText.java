package com.luckmerlin.browser;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

public class AlertText {
    private CharSequence mMessage;
    private Drawable mBackground;
    private Drawable mMessageBg;

    public CharSequence getMessage() {
        return mMessage;
    }

    public AlertText setMessage(CharSequence msg){
        mMessage=msg;
        return this;
    }

    public Drawable getBackground() {
//        View view;
        return mBackground;
    }

    public Drawable getMessageBackground() {
        return mMessageBg;
    }
}
