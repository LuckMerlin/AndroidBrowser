package com.luckmerlin.binding;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewParent;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.view.ViewIterate;
import com.luckmerlin.view.ViewIterator;
import com.merlin.model.ContentActivity;
import java.util.HashSet;
import java.util.Set;

public class ViewBinding extends ObjectBinding {
    private int mClickId=View.NO_ID;
    private boolean mEnableLongClick=false;
    private boolean mAutoFill=true;
    private OnViewBinding mOnViewBinding;
    private ImageFetcher mImage;

    public ViewBinding(Object object) {
        setObject(object);
    }

    public ViewBinding enableLongClick(boolean enable){
        mEnableLongClick=enable;
        return this;
    }

    public ViewBinding fill(boolean enable){
        mAutoFill=enable;
        return this;
    }

    public final boolean isAutoFill() {
        return mAutoFill;
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

    public ViewBinding img(int img){
        return img(img!=Resources.ID_NULL?(View view,Binding binding, ImageFetcher.OnImageFetch callback)->
                callback.onImageFetched(getResource(null!=view?view.getContext():null,img)):null);
    }

    public ViewBinding img(Bitmap img){
        return img(null!=img?(View view,Binding binding, ImageFetcher.OnImageFetch callback)-> callback.onImageFetched(img):null);
    }

    public ViewBinding img(Drawable img){
        return img(null!=img?(View view,Binding binding, ImageFetcher.OnImageFetch callback)-> callback.onImageFetched(img):null);
    }

    public ViewBinding img(ImageFetcher img){
        mImage=img;
        return this;
    }

    public Object getImage() {
        return mImage;
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
            checkClick(binding,view);
            checkFill(binding,view);
        }

        public void checkClick(Binding binding, View view){
            if (null!=binding&&null!=view&&binding instanceof ViewBinding){
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

        public void checkFill(Binding binding, View view){
            if (null==binding||!(binding instanceof ViewBinding)){
                return;
            }
            ViewBinding viewBinding=(ViewBinding)binding;
            //
            Object resObject=viewBinding.isAutoFill()?getResource(view.getContext(),viewBinding.mClickId):null;
            if (null==resObject){
               //Do nothing
            }else if (resObject instanceof Drawable||resObject instanceof Bitmap){
                setViewImage(view,resObject);
            }else if (resObject instanceof CharSequence){
                if (view instanceof TextView){
                    ((TextView)view).setText((CharSequence)resObject);
                }
            }
            //Check image fetcher
            if (view instanceof ImageView) {
                ImageFetcher imageFetcher = viewBinding.mImage;
                if (null != imageFetcher) {
                    imageFetcher.fetchImage(view, binding,(Object image) ->setViewImage(view,image));
                }
            }
        }

        public final Object getResource(Context context,int resId){
            return ViewBinding.getResource(context,resId);
        }

        public final boolean dispatchLongClick2View(View view,final View root,final int clickId,Object obj){
            return ViewBinding.dispatchLongClick2View(view,root,clickId,obj);
        }

        public final boolean dispatchClick2View(View view,final View root,final int clickId,int count,Object obj){
            return ViewBinding.dispatchClick2View(view,root,clickId,count,obj);
        }
    }

    private static boolean setViewImage(View view,Object img){
        if (null==view||null==img){
            return false;
        }
        if (view instanceof ImageView){
            if (img instanceof Drawable){
                ((ImageView)view).setImageDrawable((Drawable)img);
                return true;
            }else if (img instanceof Bitmap){
                ((ImageView)view).setImageBitmap((Bitmap)img);
                return true;
            }
        }else if (view instanceof CompoundButton){
            if (img instanceof Drawable){
                ((CompoundButton)view).setButtonDrawable((Drawable)img);
                return true;
            }
        }
        return false;
    }

    private static Object getResource(Context context,int resId){
        Resources resources=null!=context?context.getResources():null;
        if (null==resources){
            return null;
        }
        try {
            String name=resources.getResourceTypeName(resId);
            if (null==name){
                return null;
            }else if (name.equals("string")){
                return resources.getString(resId);
            }else if (name.equals("drawable")){
                return resources.getDrawable(resId);
            }
        }catch (Exception e){

        }
        return null;
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
