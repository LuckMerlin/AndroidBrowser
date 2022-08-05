package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.databinding.ObservableField;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.BrowserContentMenusBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.ViewContent;

public class MenuContextDialogContent extends ViewContent implements OnClickListener {
    private final ObservableField<String> mTitle=new ObservableField<>();

    public MenuContextDialogContent setTitle(String title){
        mTitle.set(title);
        return this;
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        removeFromParent();//Anyone view click to dismiss
        switch (clickId){
            case R.layout.browser_content_menus:
                Debug.D("WERWERQWEQ  "+clickId);
                return true;
        }
        return false;
    }

    @Override
    protected View onCreateContent(Context context) {
        BrowserContentMenusBinding binding=DataBindingUtil.inflate(context, R.layout.browser_content_menus);
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
