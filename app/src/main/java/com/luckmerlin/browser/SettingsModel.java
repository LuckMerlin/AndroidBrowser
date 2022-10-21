package com.luckmerlin.browser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import androidx.databinding.ViewDataBinding;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.SettingsModelBinding;
import com.luckmerlin.browser.settings.Settings;
import com.luckmerlin.click.OnClickListener;
import com.merlin.model.OnActivityCreate;

public class SettingsModel extends BaseModel implements OnActivityCreate, OnClickListener {
    private Settings mSettings;

    @Override
    protected View onCreateContent(Context context) {
        ViewDataBinding binding= DataBindingUtil.inflate(context,R.layout.settings_model);
        if (null!= binding&&binding instanceof SettingsModelBinding){
            ((SettingsModelBinding)binding).setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, Activity activity) {
        mSettings=Settings.Instance();
        setTitle(getString(activity,R.string.settings));
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId) {
            case R.drawable.selector_back:
                return finishActivity() || true;
            case R.string.browserLastPath:
                return (null!=mSettings&&null!=view&&view instanceof CheckBox &&
                        null!=mSettings.enableSaveLatestBrowserPath(!((CheckBox)view).isChecked()))||true;
        }
        return false;
    }
}
