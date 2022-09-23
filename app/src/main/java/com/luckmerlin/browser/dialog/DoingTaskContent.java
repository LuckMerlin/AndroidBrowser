package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import androidx.databinding.ObservableField;

import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.databinding.DoingTaskBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Brief;
import com.luckmerlin.core.Named;
import com.luckmerlin.task.Progress;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.ViewIterator;

public class DoingTaskContent extends BaseContent implements OnClickListener {
    private final ObservableField<String> mName=new ObservableField<>();
    private final ObservableField<Brief> mFrom=new ObservableField<>();
    private final ObservableField<Brief> mTo=new ObservableField<>();

    public DoingTaskContent(){
        mName.set("沙发大厦发放单独发搭搭撒撒代发大萨达发发发大");
        mFrom.set(new Brief() {
            @Override
            public CharSequence getTitle() {
                return "沙发大厦发放单独发搭搭撒撒代发大萨达发发发大";
            }

            @Override
            public CharSequence getSubTitle() {
                return "沙发大厦发放单独发搭搭撒撒代发大萨达发发发大";
            }

            @Override
            public Object getIcon() {
                return R.drawable.hidisk_icon_aac;
            }
        });
        mTo.set(mFrom.get());
    }

    @Override
    protected View onCreateContent(Context context) {
        DoingTaskBinding binding=inflate(context,R.layout.doing_task);
        binding.setDoing(new DoingTaskContent());
        return binding.getRoot();
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.string.sure: return removeFromParent()||true;
            case R.string.cancel: return removeFromParent()||true;
        }
        return false;
    }

    public ObservableField<Progress> getProgress(){
        return null;
    }

    public ObservableField<String> getName(){
        return mName;
    }

    public ObservableField<Brief> getFrom() {
        return mFrom;
    }

    public ObservableField<Brief> getTo() {
        return mTo;
    }
}
