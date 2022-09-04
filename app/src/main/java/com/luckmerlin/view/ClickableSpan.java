package com.luckmerlin.view;

import android.view.View;

public class ClickableSpan extends android.text.style.ClickableSpan {
    private OnSpanClick mOnSpanClick;

    public interface OnSpanClick{
        void onSpanClick(View widget);
    }

    public ClickableSpan(){
        this(null);
    }

    public ClickableSpan(OnSpanClick callback){
        setOnSpanClick(callback);
    }

    public ClickableSpan setOnSpanClick(OnSpanClick callback){
        mOnSpanClick=callback;
        return this;
    }

    @Override
    public void onClick(View widget) {
        OnSpanClick onSpanClick=mOnSpanClick;
        if (null!=onSpanClick){
            onSpanClick.onSpanClick(widget);
        }
    }
}
