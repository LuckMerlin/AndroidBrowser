package com.luckmerlin.browser.binding;

import android.view.View;
import android.view.ViewParent;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.view.ViewIterator;

public class VB extends ObjectBinding{
    private OnClickListener mOnClickListener;
    private OnLongClickListener mLongClickListener;
    private int mClickId=View.NO_ID;
    private boolean mEnableLongClick=false;

    public VB(Object object) {
        super(object);
    }

    public VB setListener(OnClickListener listener){
        mOnClickListener=listener;
        return this;
    }

    public VB enableLongClick(boolean enable){
        mEnableLongClick=enable;
        return this;
    }

    public static VB create(int clickId){
        return create(null).setClickId(clickId);
    }

    public static VB clickId(int clickId){
        return clickId(clickId,null);
    }

    public static VB clickId(int clickId, Object obj){
        return new VB(obj).setClickId(clickId);
    }

    public static VB create(Object obj){
        return new VB(obj);
    }

    public VB setClickId(int id){
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
        return iterate(view, (Object child)-> child instanceof OnLongClickListener&&
                ((OnLongClickListener)child).onLongClick(root,clickId,obj));
    }

    private boolean dispatchClick2View(View view,final View root,final int clickId,int count,Object obj){
        return iterate(view, (Object child)-> child instanceof OnClickListener&&
                ((OnClickListener)child).onClick(root,clickId,count,obj));
    }

    private boolean iterate(Object obj,Iterator iterator){
        if (null==obj||null==iterator){
            return false;
        }else if (iterator.iterate(obj)){
            return true;
        }else if (obj instanceof ViewIterator){
            return iterator.iterate(((ViewIterator)obj).onIterateView());
        }else if (obj instanceof View){
            View view=(View)obj;
            if (iterate(view.getContext(),iterator)){
                return true;
            }
            ViewParent parent=view.getParent();
            if (null!=parent&&parent instanceof View){
                return iterate((View) parent,iterator);
            }
        }
        return false;
    }

    private interface Iterator{
        boolean iterate(Object obj);
    }
}
