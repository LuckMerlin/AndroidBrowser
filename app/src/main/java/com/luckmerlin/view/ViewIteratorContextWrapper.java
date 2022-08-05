package com.luckmerlin.view;

import android.content.Context;
import android.content.ContextWrapper;

public abstract class ViewIteratorContextWrapper extends ContextWrapper implements ViewIterator {

    public ViewIteratorContextWrapper(Context base) {
        super(base);
    }
}
