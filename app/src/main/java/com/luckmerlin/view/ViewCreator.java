package com.luckmerlin.view;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

public class ViewCreator {

    public View inflate(Context context,Object obj){
        if (null==obj){
            return null;
        }else if (obj instanceof View){
            return (View)obj;
        }
        Resources resources=null!=context?context.getResources():null;
        if (null==resources){
            return null;
        }else if (obj instanceof Integer){
            try{
                String name=resources.getResourceTypeName((Integer)obj);
            }catch (Exception e){
            }
        }
        TextView textView=new TextView(context);
        textView.setText(""+obj);
        return textView;
    }
}
