package com.luckmerlin.browser.binding;

import android.content.Context;
import android.view.View;
import android.view.ViewParent;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;

public class ClickBinding extends ObjectBinding{
    private OnClickListener mOnClickListener;
    private OnLongClickListener mLongClickListener;
    private int mClickId=View.NO_ID;
    private boolean mEnableLongClick=false;

    public ClickBinding(Object object) {
        super(object);
    }

    public ClickBinding setListener(OnClickListener listener){
        mOnClickListener=listener;
        return this;
    }

    public ClickBinding enableLongClick(boolean enable){
        mEnableLongClick=enable;
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
            if (mEnableLongClick){
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int clickId=mClickId;
                        return preferLongClick(v,clickId==View.NO_ID?v.getId():clickId,getObject(),mLongClickListener);
                    }
                });
            }
        }
    }

    private boolean preferClick(View view,int clickId,int count,Object obj,OnClickListener clickListener){
        if (null==view){
            return false;
        }
        if (null!=clickListener&&clickListener.onClick(view,clickId,count,obj)){
            return true;
        }
        return dispatchClick2View(view,view,clickId,count,obj);
    }

    private boolean preferLongClick(View view,int clickId,Object obj,OnLongClickListener clickListener){
        if (null==view){
            return false;
        }
        if (null!=clickListener&&clickListener.onLongClick(view,clickId,obj)){
            return true;
        }
        return dispatchLongClick2View(view,view,clickId,obj);
    }

    private boolean dispatchLongClick2View(View view,final View root,final int clickId,Object obj){
        if (null==view){
            return false;
        }else if (view instanceof OnLongClickListener&&((OnLongClickListener)view).onLongClick(root,clickId,obj)){
            return true;
        }
        ViewParent parent=view.getParent();
        if (null!=parent&&parent instanceof View){
            return dispatchLongClick2View((View) parent,root,clickId,obj);
        }
        Context context=null!=root?root.getContext():null;
        if (null==context){
            return false;
        }else if (context instanceof OnLongClickListener&&((OnLongClickListener)context).onLongClick(root,clickId,obj)){
            return true;
        }
        return false;
    }

    private boolean dispatchClick2View(View view,final View root,final int clickId,int count,Object obj){
        if (null==view){
            return false;
        }else if (view instanceof OnClickListener&&((OnClickListener)view).onClick(root,clickId,count,obj)){
            return true;
        }
        ViewParent parent=view.getParent();
        if (null!=parent&&parent instanceof View){
            return dispatchClick2View((View) parent,root,clickId,count,obj);
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
