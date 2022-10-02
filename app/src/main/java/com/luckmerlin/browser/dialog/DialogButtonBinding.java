package com.luckmerlin.browser.dialog;

import android.view.View;
import android.view.ViewGroup;
import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.ButtonTextBinding;
import com.luckmerlin.click.Listener;

public class DialogButtonBinding implements Binding {
    private ViewBinding[] mBindings;
    private Listener mListener;

    public DialogButtonBinding(ViewBinding ...bindings){
        setButton(bindings);
    }

    public final DialogButtonBinding setButton(ViewBinding ...bindings){
        mBindings=bindings;
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
        ViewBinding[] bindings=mBindings;
        if (null==bindings){
            return;
        }
        for (ViewBinding child:bindings) {
            ButtonTextBinding binding=DataBindingUtil.inflate(view.getContext(),
                    R.layout.button_text,(ViewGroup)view,true);
            binding.setBinding(null==child?null:(null!=mListener?child.setListener(mListener):child));
        }
    }
}
