package com.luckmerlin.browser;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.luckmerlin.browser.databinding.ItemBrowserFileBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.merlin.adapter.PageListAdapter;
import java.util.List;

public class BrowserListAdapter extends PageListAdapter<BrowseQuery,File> {
    private ObservableField<Long> mCurrentSelectSize=new ObservableField<Long>();
    private ObservableField<Mode> mBrowserMode=new ObservableField<Mode>();
    private ObservableField<Boolean> mPageLoading=new ObservableField<Boolean>();

    protected BrowserListAdapter(PageLoader<BrowseQuery,File> pageLoader) {
        setPageLoader(pageLoader);
    }

    protected BrowserListAdapter(DiffUtil.ItemCallback diffCallback) {
        super(diffCallback);
    }

    protected BrowserListAdapter(AsyncDifferConfig config) {
        super(config);
    }

    public final ObservableField<Long> getCurrentSelectSize() {
        return mCurrentSelectSize;
    }

    public final boolean setFolder(BrowseQuery folder){
        return setFolder(folder,false);
    }

    public final boolean setFolder(BrowseQuery folder,boolean force){
        if (null!=folder){
            BrowseQuery current=getCurrent();
            if (!force&&null!=current&&current.equals(folder)){
                return false;
            }
            return reset(folder,-1,null);
        }
        return false;
    }

    @Override
    protected void onPageLoadingChange(boolean loading) {
        super.onPageLoadingChange(loading);
        mPageLoading.set(loading);
    }

    @Override
    protected Object onCreateDataViewHolder(ViewGroup parent) {
        return R.layout.item_browser_file;
    }

    @Override
    protected void onBindData(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        View itemView=null!=holder?holder.itemView:null;
        ViewDataBinding binding=null!=itemView? DataBindingUtil.getBinding(itemView):null;
        if (null!=binding&&binding instanceof ItemBrowserFileBinding){
            ItemBrowserFileBinding fileBinding=((ItemBrowserFileBinding)binding);
            File file=getItem(position);
            fileBinding.setPath(file);
            fileBinding.setMode(mBrowserMode.get());
            fileBinding.setIcon(itemView.getResources().getDrawable(R.drawable.hidisk_icon_folder));
        }
    }

    public boolean isAllChoose(){
        return false;
    }

    public final ObservableField<Mode> getBrowserMode() {
        return mBrowserMode;
    }

    public ObservableField<Boolean> getPageLoading() {
        return mPageLoading;
    }
}
