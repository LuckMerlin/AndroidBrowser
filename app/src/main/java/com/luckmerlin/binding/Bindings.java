package com.luckmerlin.binding;

import android.view.View;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bindings implements Binding{
    private List<Binding> mBindings;

    public Bindings(Binding ...bindings){
        add(bindings);
    }

    @Override
    public void onBind(View view) {
        List<Binding> bindings=mBindings;
        if (null!=bindings){
            for (Binding binding:bindings) {
                if (null!=binding){
                    binding.onBind(view);
                }
            }
        }
    }

    public final Bindings add(Binding ...bindings){
        List<Binding> input=null!=bindings?Arrays.asList(bindings):null;
        if (null==input||input.size()<=0){
            return this;
        }
        List<Binding> bindingList=mBindings;
        bindingList=null!=bindingList?bindingList:(mBindings=new ArrayList<>());
        bindingList.addAll(input);
        return this;
    }

    public final Bindings remove(Binding binding){
        List<Binding> bindings=null!=binding?mBindings:null;
        if (null!=bindings&&bindings.remove(binding)){
            //Do nothing
        }
        return this;
    }

    public final Bindings clean(){
        mBindings=null;
        return this;
    }
}
