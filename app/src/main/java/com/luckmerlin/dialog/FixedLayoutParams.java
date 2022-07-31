package com.luckmerlin.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.luckmerlin.view.LayoutParamsResolver;

public class FixedLayoutParams implements LayoutParamsResolver {
    public Number mWidth;
    public Number mHeight;
    public int mGravity= Gravity.NO_GRAVITY;

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
        return new int[]{computeSize(mWidth,full[0]),computeSize(mHeight,full[1])};
    }

    private int computeSize(Number size,int full){
        if (null==size){
            return ViewGroup.LayoutParams.MATCH_PARENT;
        }else if(size instanceof Integer){
            return (Integer)size;
        }else if(size instanceof Long){
            return ((Long)size).intValue();
        }else if(size instanceof Double){
            return computeSize(((Double)size).floatValue(),full);
        }else if (size instanceof Float){
            return (int)(full*(Float)size);
        }
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

}
