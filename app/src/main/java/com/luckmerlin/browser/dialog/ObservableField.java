package com.luckmerlin.browser.dialog;

public class ObservableField<T> extends androidx.databinding.ObservableField<T> {

    @Override
    public void set(T value) {
        T current=get();
        if (value != current||(null!=current&&!value.equals(current))) {
            super.set(value);
            notifyChange();
        }
    }
}
