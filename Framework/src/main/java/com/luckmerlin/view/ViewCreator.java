package com.luckmerlin.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.luckmerlin.debug.Debug;

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
        }else if (obj instanceof String){
            TextView textView=new TextView(context);
            textView.setTextColor(Color.WHITE);
            textView.setText((String)obj);
            return textView;
        }else if (obj instanceof ViewDataBinding){
            return ((ViewDataBinding)obj).getRoot();
        }else if (obj instanceof Drawable){
            ImageView imageView=new ImageView(context);
            imageView.setImageDrawable((Drawable)obj);
            return inflate(context,imageView);
        }else if (obj instanceof Bitmap){
            return inflate(context,new BitmapDrawable(null,(Bitmap) obj));
        }else if (obj instanceof Integer){
            try{
                String name=resources.getResourceTypeName((Integer)obj);
                if (null==name){
                    return inflate(context,""+obj);
                }else if (name.equals("string")){
                    return inflate(context,resources.getString((Integer)obj));
                }else if (name.equals("drawable")){
                    return inflate(context,resources.getDrawable((Integer)obj));
                }else if (name.equals("layout")){
                    try{
                        ViewDataBinding binding= DataBindingUtil.inflate(LayoutInflater.from(context),(Integer)obj,null,true);
                        return inflate(context,binding);
                    }catch (Exception e){
                        //Do nothing
                    }
                    return inflate(context,LayoutInflater.from(context) .inflate((Integer)obj,null,true));
                }
            }catch (Exception e){
            }
        }
        return inflate(context,""+obj);
    }
}
