package com.luckmerlin.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.LayoutParamsResolver;

public class FixedLayoutParams implements LayoutParamsResolver {
    public Number mWidth;
    public Number mHeight;
    public int mGravity= Gravity.NO_GRAVITY;
    public Number mMaxWidth;
    public Number mMaxHeight;

    public FixedLayoutParams(){
        this(null,null);
    }

    public FixedLayoutParams(Number width, Number height){
        this(width,height,Gravity.NO_GRAVITY);
    }

    public FixedLayoutParams(Number width, Number height, int gravity){
        mWidth=width;
        mHeight=height;
        mGravity=gravity;
    }

    public FixedLayoutParams setMaxHeight(Number maxHeight) {
        this.mMaxHeight = maxHeight;
        return this;
    }

    public FixedLayoutParams setMaxWidth(Number maxWidth) {
        this.mMaxWidth = maxWidth;
        return this;
    }

    public FixedLayoutParams dialog(){
        mWidth=ViewGroup.LayoutParams.WRAP_CONTENT;
        mHeight=ViewGroup.LayoutParams.WRAP_CONTENT;
        mGravity=Gravity.CENTER;
        mMaxHeight=0.5f;
        return this;
    }

    @Override
    public void onResolveLayoutParams(Context context,ViewGroup.LayoutParams params) {
        if (null!=params){
            int[] size=getLayoutSize(context);
            if (null!=size&&size.length>=2){
                params.width=size[0];
                params.height=size[1];
            }
            if (params instanceof FrameLayout.LayoutParams){
                ((FrameLayout.LayoutParams)params).gravity=mGravity;
            }
        }
    }

    public final int[] getLayoutSize(Context context){
        Resources resources=null!=context?context.getResources():null;
        DisplayMetrics metrics=null!=resources?resources.getDisplayMetrics():null;
        return getLayoutSize(null!=metrics?new int[]{metrics.widthPixels,metrics.heightPixels}:null);
    }

    public final int[] getLayoutSize(int[] full){
        if (null==full||full.length<2){
            return new int[]{ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT};
        }
        int maxWidth=computeSize(mMaxWidth,0,full[0]);
        int maxHeight=computeSize(mMaxHeight,0,full[1]);
        return new int[]{computeSize(mWidth,maxWidth,full[0]), computeSize(mHeight,maxHeight,full[1])};
    }

    private int computeSize(Number size,int max,int full){
        if (null==size){
            if (max>0){
                return View.MeasureSpec.makeMeasureSpec(max, View.MeasureSpec.AT_MOST);
            }
            return ViewGroup.LayoutParams.MATCH_PARENT;
        }else if(size instanceof Integer){
            if (max>0){
                if (((Integer)size)<0||(((Integer)size)>0&&((Integer)size)>max)){
                    return View.MeasureSpec.makeMeasureSpec(max, View.MeasureSpec.AT_MOST);
                }
            }
            return (Integer)size;
        }else if(size instanceof Long){
            return ((Long)size).intValue();
        }else if(size instanceof Double){
            return computeSize(((Double)size).floatValue(),max,full);
        }else if (size instanceof Float){
            return (int)(full*(Float)size);
        }
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

}
