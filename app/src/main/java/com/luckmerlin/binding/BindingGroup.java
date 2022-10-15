package com.luckmerlin.binding;

import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class BindingGroup implements Binding{
    private List<Binding> mBindings;

    public BindingGroup(Binding ...bindings){
        add(bindings);
    }

    public final BindingGroup add(Binding ...bindingsArg){
        if (null!=bindingsArg&&bindingsArg.length>0){
            for (Binding child:bindingsArg) {
                if(null!=child){
                    List<Binding> bindings=mBindings;
                    (null!=bindings?bindings:(mBindings=new ArrayList<>())).add(child);
                }
            }
        }
        return this;
    }

    @Override
    public void onBind(View view) {
        if (null!=view){
            List<Binding> bindings=mBindings;
            if (null!=bindings){
                for (Binding child:bindings) {
                    child.onBind(view);
                }
            }
        }
    }
}
