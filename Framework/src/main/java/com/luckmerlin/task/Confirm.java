package com.luckmerlin.task;

import com.luckmerlin.core.Brief;

public class Confirm extends BindingResult implements Brief,Doing {
    private String mName;
    private String mTitle;

    public final Confirm setName(String name) {
        mName = name;
        return this;
    }

    public final Confirm setTitle(String title) {
        mTitle = title;
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

    public String getTitle() {
        return mTitle;
    }
}
