package com.luckmerlin.browser.binding;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.view.ViewParent;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.ViewIterate;
import com.luckmerlin.view.ViewIterator;
import com.merlin.model.ContentActivity;

import java.util.HashSet;
import java.util.Set;

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
                ((OnLongClickListener)child).onLongClick(root,clickId,obj),null);
    }

    private boolean dispatchClick2View(View view,final View root,final int clickId,int count,Object obj){
        return iterate(view, (Object child)-> child instanceof OnClickListener&&
                ((OnClickListener)child).onClick(root,clickId,count,obj),null);
    }

    private boolean iterate(Object obj, ViewIterate iterator, Set<Object> iterated){
        if (null==obj||null==iterator){
            return false;
        }
        iterated=null!=iterated?iterated:new HashSet<>();
        if (makeIterate(obj,iterator,iterated)){
            return true;
        }
        if (obj instanceof View){
            View view=(View)obj;
            Context context=view.getContext();
            if (null!=context&&context instanceof ViewIterator&&makeIterate(context,iterator,iterated)){
                return true;
            }
            ViewParent parent=view.getParent();
            if (null!=parent&&parent instanceof View) {
                return iterate((View) parent, iterator, iterated);
            }else if (null!=context){
                context=null!=context&&context instanceof ContextWrapper?((ContextWrapper)context).getBaseContext():context;
                if (context instanceof Activity){
                    if (makeIterate(context,iterator,iterated)){
                        return true;
                    }else if (context instanceof ContentActivity&&makeIterate(((ContentActivity)context).getContent(),iterator,iterated)){
                        return true;
                    }
                }
                return makeIterate(context.getApplicationContext(),iterator,iterated);
            }
        }
        return false;
    }

    private boolean makeIterate(Object obj, ViewIterate iterator, Set<Object> iterated){
        if (null!=obj&&null!=iterator){
            if (null==iterated){
                if (iterator.iterate(obj)){
                    return true;
                }else if (obj instanceof ViewIterator&&((ViewIterator)obj).onViewIterate(iterator)){
                    return true;
                }
            }else if (!iterated.contains(obj)){
                iterated.add(obj);
                if (iterator.iterate(obj)){
                    return true;
                }else if (obj instanceof ViewIterator&&((ViewIterator)obj).onViewIterate(iterator)){
                    return true;
                }
            }
        }
        return false;
    }

}
