package com.luckmerlin.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.luckmerlin.view.LayoutParamsResolver;

public class PopupWindow extends AbstractWindow {
    private android.widget.PopupWindow mPopupWindow;

    public PopupWindow(Context context) {
        super(context);
    }

    @Override
    public boolean dismiss() {
        android.widget.PopupWindow popupWindow=mPopupWindow;
        if (null!=popupWindow){
            popupWindow.dismiss();
            return true;
        }
        return super.dismiss();
    }

    public boolean showAsDropDown(View view, int left, int top, int gravity){
        return showAsDropDown(view,null,left,top,gravity,true);
    }

    public boolean showAsDropDown(View view, LayoutParamsResolver resolver, int left, int top, int gravity){
        return showAsDropDown(view,resolver,left,top,gravity,true);
    }

    public boolean showAsDropDown(View view, LayoutParamsResolver resolver, int left, int top, int gravity,boolean focusable){
        if (null==view){
            return false;
        }
        View root=getRoot();
        if (null==root||null!=root.getParent()){
            return false;
        }
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.
                LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resolveLayoutParams(root.getContext(),layoutParams,resolver);
        android.widget.PopupWindow popupWindow=mPopupWindow=new
                android.widget.PopupWindow(root,layoutParams.width,layoutParams.height,focusable);
        popupWindow.setOnDismissListener(()-> {
            android.widget.PopupWindow current =mPopupWindow;
            if (null!=current&&current==popupWindow){
                mPopupWindow=null;
            }
        });
        popupWindow.showAsDropDown(view,left,top,gravity);
        return true;
    }

    public boolean showAtLocation(View parent,int left, int top, int gravity){
        return showAtLocation(parent,null,left,top,gravity,true);
    }

    public boolean showAtLocation(View parent,LayoutParamsResolver resolver,int left, int top, int gravity){
        return showAtLocation(parent,resolver,left,top,gravity,true);
    }

    public boolean showAtLocation(View parent,LayoutParamsResolver resolver,int left, int top, int gravity,boolean focusable){
        if (null==parent){
            return false;
        }
        View root=getRoot();
        if (null==root||null!=root.getParent()){
            return false;
        }
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.
                LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        resolveLayoutParams(root.getContext(),layoutParams,resolver);
        android.widget.PopupWindow popupWindow=mPopupWindow=new
                android.widget.PopupWindow(root,layoutParams.width,layoutParams.height,focusable);
        popupWindow.setOnDismissListener(()-> {
            android.widget.PopupWindow current =mPopupWindow;
            if (null!=current&&current==popupWindow){
                mPopupWindow=null;
            }
        });
        popupWindow.showAtLocation(parent,left,top,gravity);
        return true;
    }
}
