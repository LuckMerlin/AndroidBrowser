package com.luckmerlin.task;

import com.luckmerlin.core.Brief;

@Deprecated
public class Confirm1 extends BindingResult implements Brief {
    private String mName;
    private String mTitle;

    public final Confirm1 setName(String name) {
        mName = name;
        return this;
    }

    public final Confirm1 setTitle(String title) {
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
