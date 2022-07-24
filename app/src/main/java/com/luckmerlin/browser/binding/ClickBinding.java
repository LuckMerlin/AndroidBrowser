package com.luckmerlin.browser.binding;

import android.content.Context;
import android.view.View;
import android.view.ViewParent;
import com.luckmerlin.click.OnClickListener;

public class ClickBinding extends ObjectBinding{
    private OnClickListener mOnClickListener;
    private int mClickId=View.NO_ID;

    public ClickBinding(Object object) {
        super(object);
    }

    public ClickBinding setListener(OnClickListener listener){
        mOnClickListener=listener;
        return this;
    }

    public static ClickBinding create(Object obj){
        return new ClickBinding(obj);
    }

    public ClickBinding setClickId(int id){
        mClickId=id;
        return this;
    }

    @Override
    public void onBind(View view) {
        if (null!=view){
            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int clickId=mClickId;
                    preferClick(v,clickId==View.NO_ID?v.getId():clickId,1,getObject(),mOnClickListener);
                }
            });
        }
    }

    private boolean preferClick(View view,int clickId,int count,Object obj,OnClickListener clickListener){
        if (null==view){
            return false;
        }
        if (null!=clickListener&&clickListener.onClick(view,clickId,count,obj)){
            return true;
        }
        return dispatch2View(view,view,clickId,count,obj);
    }

    private boolean dispatch2View(View view,final View root,final int clickId,int count,Object obj){
        if (null==view){
            return false;
        }else if (view instanceof OnClickListener&&((OnClickListener)view).onClick(root,clickId,count,obj)){
            return true;
        }
        ViewParent parent=view.getParent();
        if (null!=parent&&parent instanceof View){
            return dispatch2View((View) parent,root,clickId,count,obj);
        }
        Context context=null!=root?root.getContext():null;
        if (null==context){
            return false;
        }else if (context instanceof OnClickListener&&((OnClickListener)context).onClick(root,clickId,count,obj)){
            return true;
        }
        return false;
    }
}
