package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.Bindings;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.DialogContentBinding;
import com.luckmerlin.browser.databinding.DialogMessageBinding;
import com.luckmerlin.browser.databinding.DoingTaskBinding;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.CodeResult;
import com.luckmerlin.core.MessageResult;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.BindingResult;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskRestartEnabler;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.ViewIterate;
import com.luckmerlin.view.ViewIterator;

public class DialogContent extends BaseContent implements OnClickListener{
    private final ObservableField<String> mTitle=new ObservableField<>();
    private final ObservableField<Binding> mButtonBinding=new ObservableField<>();
    private final ObservableField<Binding> mDialogBinding=new ObservableField<>();

    @Override
    protected View onCreateContent(Context context) {
        DialogContentBinding binding=inflate(context,R.layout.dialog_content);
        binding.setContent(this);
        return binding.getRoot();
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        return removeFromParent()||true;
    }

    public final DialogContent setDialogBinding(Binding binding){
        if (!isUiThread()){
            post(()->setContentBinding(binding));
            return this;
        }
        mDialogBinding.set(binding);
        return this;
    }

    public final DialogContent setButtonBinding(Binding binding){
        if (!isUiThread()){
            post(()->setButtonBinding(binding));
            return this;
        }
        mButtonBinding.set(binding);
        return this;
    }

    public final DialogContent setMessage(CharSequence message){
        return setDialogContent(message);
    }

    public final DialogContent setDialogContent(Object object){
        if (!isUiThread()){
            post(()->setDialogContent(object));
            return this;
        }
        if (null==object){
            mDialogBinding.set(null);
            return this;
        }else if (object instanceof Binding){
            mDialogBinding.set((Binding)object);
            return this;
        }else if (object instanceof CharSequence){
            return setDialogContent((Binding)(View view)->((DialogMessageBinding)DataBindingUtil.inflate((ViewGroup)view,
                    R.layout.dialog_message,true)).setMessage((CharSequence)object));
        }else if (object instanceof View){
            return setDialogContent((Content)(Context context, ViewIterator iterator)->(View)object);
        }else if (object instanceof Content){
            return setDialogContent((Binding) (View parent) ->{
                if (null!=parent&&parent instanceof ViewGroup){
                    View view=((Content)object).onCreateContentView(parent.getContext(),(ViewIterate iterate)->
                            null!=iterate&&iterate.iterate(DialogContent.this));
                    if (null==view.getParent()){
                        ((ViewGroup)parent).addView(view);
                    }
                }
            });
        }
        return this;
    }

    public final DialogContent setTitle(String name){
        mTitle.set(name);
        return this;
    }

    public final ObservableField<String> getTitle(){
        return mTitle;
    }

    public ObservableField<Binding> getButtonBinding() {
        return mButtonBinding;
    }

    public ObservableField<Binding> getDialogBinding() {
        return mDialogBinding;
    }
}
