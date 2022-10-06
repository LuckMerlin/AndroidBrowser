package com.luckmerlin.browser;

import android.graphics.drawable.Drawable;
import android.text.method.MovementMethod;

public class AlertText {
    private CharSequence mMessage;
    private Drawable mBackground;
    private Drawable mMessageBg;
    private MovementMethod mMovementMethod;

    public CharSequence getMessage() {
        return mMessage;
    }

    public AlertText setMessage(CharSequence msg){
        mMessage=msg;
        return this;
    }

    public AlertText setMovementMethod(MovementMethod movementMethod) {
        this.mMovementMethod = movementMethod;
        return this;
    }

    public Drawable getBackground() {
        return mBackground;
    }

    public MovementMethod getMovementMethod() {
        return mMovementMethod;
    }

    public Drawable getMessageBackground() {
        return mMessageBg;
    }
}
