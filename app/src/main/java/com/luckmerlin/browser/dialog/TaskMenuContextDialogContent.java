package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import androidx.databinding.ObservableField;

import com.luckmerlin.binding.DataBindingUtil;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.TaskContentMenusBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.view.ViewContent;

public class TaskMenuContextDialogContent extends ViewContent implements OnClickListener {
    private final ObservableField<String> mTitle=new ObservableField<>();

    public TaskMenuContextDialogContent setTitle(String title){
        mTitle.set(title);
        return this;
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        removeFromParent();//Anyone view click to dismiss
        return false;
    }

    @Override
    protected View onCreateContent(Context context) {
        TaskContentMenusBinding binding= DataBindingUtil.inflate(context, R.layout.task_content_menus);
        if (null!=binding){
            binding.setContent(this);
            return binding.getRoot();
        }
        return null;
    }

    public final ObservableField<String> getTitle() {
        return mTitle;
    }
}
