package com.luckmerlin.browser.binding;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.view.ViewParent;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.view.ViewIterate;
import com.luckmerlin.view.ViewIterator;
import com.merlin.model.ContentActivity;

import java.util.HashSet;
import java.util.Set;

public class ViewBinding extends ObjectBinding{
    private int mClickId=View.NO_ID;
    private boolean mEnableLongClick=false;
    private boolean mAutoFill=true;
    private OnViewBinding mOnViewBinding;

    public ViewBinding(Object object) {
        super(object);
    }

    public ViewBinding enableLongClick(boolean enable){
        mEnableLongClick=enable;
        return this;
    }

    public ViewBinding setBinding(OnViewBinding binding){
        mOnViewBinding=binding;
        return this;
    }

    public static ViewBinding create(int clickId){
        return create(null).setClickId(clickId);
    }

    public static ViewBinding clickId(int clickId){
        return clickId(clickId,null);
    }

    public static ViewBinding clickId(int clickId, Object obj){
        return new ViewBinding(obj).setClickId(clickId);
    }

    public static ViewBinding create(Object obj){
        return new ViewBinding(obj);
    }

    public ViewBinding setClickId(int id){
        mClickId=id;
        return this;
    }

    @Override
    public void onBind(View view) {
        if (null!=view){
            OnViewBinding onViewBinding=mOnViewBinding;
            (null!=onViewBinding?onViewBinding:(mOnViewBinding=new DefaultViewBinding())).onViewBinding(this,view);
        }
    }

    public static class DefaultViewBinding implements OnViewBinding{

        @Override
        public void onViewBinding(Binding binding, View view) {
            if (null==binding||null==view){
                return;
            }
            if (binding instanceof ViewBinding){
                ViewBinding viewBinding=(ViewBinding)binding;
                final int clickId=viewBinding.mClickId;
                final Object object=viewBinding.getObject();
                view.setOnClickListener((View v)->dispatchClick2View(v,v,
                        clickId==View.NO_ID?v.getId():clickId,1,object));
                if (viewBinding.mEnableLongClick){
                    view.setOnLongClickListener((View v)-> dispatchLongClick2View(v,v,
                            clickId==View.NO_ID?v.getId():clickId,object));
                }
            }
        }
    }

    private static boolean dispatchLongClick2View(View view,final View root,final int clickId,Object obj){
        return iterate(view, (Object child)-> child instanceof OnLongClickListener&&
                ((OnLongClickListener)child).onLongClick(root,clickId,obj),null);
    }

    private static boolean dispatchClick2View(View view,final View root,final int clickId,int count,Object obj){
        return iterate(view, (Object child)-> child instanceof OnClickListener&&
                ((OnClickListener)child).onClick(root,clickId,count,obj),null);
    }

    private static boolean iterate(Object obj, ViewIterate iterator, Set<Object> iterated){
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

    private static boolean makeIterate(Object obj, ViewIterate iterator, Set<Object> iterated){
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
