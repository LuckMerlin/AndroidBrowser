package com.luckmerlin.browser.dialog;

import android.view.View;
import android.view.ViewGroup;
import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.DataBindingUtil;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.ButtonTextBinding;
import com.luckmerlin.click.Listener;
import java.util.ArrayList;
import java.util.List;

public class DialogButtonBinding implements Binding {
    private List<ViewBinding> mBindings;
    private Listener mListener;

    public DialogButtonBinding(ViewBinding ...bindings){
        add(bindings);
    }

    public final DialogButtonBinding add(ViewBinding ...bindingsArg){
        if (null!=bindingsArg&&bindingsArg.length>0){
            for (ViewBinding child:bindingsArg) {
                if(null!=child){
                    List<ViewBinding> bindings=mBindings;
                    (null!=bindings?bindings:(mBindings=new ArrayList<>())).add(child);
                }
            }
        }
        return this;
    }

    public final DialogButtonBinding setListener(Listener listener){
        mListener=listener;
        return this;
    }

    @Override
    public void onBind(View view) {
        if (null==view||!(view instanceof ViewGroup)){
            return;
        }
        List<ViewBinding> bindings=mBindings;
        if (null==bindings){
            return;
        }
        for (ViewBinding child:bindings) {
            ButtonTextBinding binding= DataBindingUtil.inflate(view.getContext(),
                    R.layout.button_text,(ViewGroup)view,true);
            binding.setBinding(null==child?null:(null!=mListener?child.setListener(mListener):child));
        }
    }
}
