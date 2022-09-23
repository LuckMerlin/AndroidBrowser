package com.luckmerlin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView {
    private int current;
    private boolean mVertical=false;

    public MarqueeTextView(Context context) {
        this(context,null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mVertical){
            int height=getMeasuredHeight();
            scrollTo(0,current++);
            current=current>=height?-height:current;
        }else{
            int width=canvas.getWidth();
            scrollTo(current++,0);
            current=current>=width?-width:current;
        }
        postInvalidateDelayed(1000);
    }
}
