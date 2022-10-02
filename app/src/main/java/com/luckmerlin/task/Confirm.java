package com.luckmerlin.task;

import com.luckmerlin.core.Brief;

public class Confirm extends BindingResult implements Brief {
    private String mName;

    public final BindingResult setName(String name) {
        mName = name;
        return this;
    }

    @Override
    public final CharSequence getName() {
        return mName;
    }

    @Override
    public CharSequence getNote() {
        return getMessage();
    }

    @Override
    public Object getIcon() {
        return null;
    }

}
