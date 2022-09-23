package com.luckmerlin.browser;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.databinding.ItemBrowserFileBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.merlin.adapter.PageListAdapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class BrowserListAdapter extends PageListAdapter<BrowseQuery,File> {
    private ObservableField<Long> mCurrentSelectSize=new ObservableField<Long>();
    private ObservableField<Boolean> mPageLoading=new ObservableField<Boolean>();
    private ObservableField<Client> mBrowserClient=new ObservableField<>();
    private ObservableField<Folder> mCurrentFolder=new ObservableField<>();
    private final Map<RecyclerView.ViewHolder,Canceler> mThumbLoading=new HashMap<>();
    private ObservableField<CharSequence> mCurrentPath=new ObservableField<>();
    private final PathSpanClick mPathSpanClick=new PathSpanClick();
    private Mode mMode;
    private Executor mExecutor;

    protected BrowserListAdapter() {
        setPageSize(50);
        setFixedHolder(VIEW_TYPE_EMPTY,R.layout.item_content_empty);
    }

    protected BrowserListAdapter(DiffUtil.ItemCallback diffCallback) {
        super(diffCallback);
    }

    protected BrowserListAdapter(AsyncDifferConfig config) {
        super(config);
    }

    public BrowserListAdapter setExecutor(Executor executor) {
        this.mExecutor = executor;
        return this;
    }

    private boolean execute(Runnable runnable){
        if (null==runnable){
            return false;
        }
        Executor executor=mExecutor;
        executor=null!=executor?executor:(mExecutor= Executors.newCachedThreadPool((Runnable r) ->{
            Thread thread=new Thread(r);
            thread.setName(BrowserListAdapter.class.getName());
            return thread;
        }));
        executor.execute(runnable);
        return true;
    }

    @Override
    protected Canceler onPageLoad(BrowseQuery args,int fromIndex, File from, int pageSize, OnPageLoadListener<File> callback) {
        if (null==callback){
            return ()->false;
        }
        Client client=getClient();
        if (null==client){
            return ()->false;
        }
        boolean[] canceled=new boolean[]{false};
        execute(()->{
            Response<Folder> response=client.listFiles(null!=args?args.mFolder:null,fromIndex,pageSize,null);
            if (!canceled[0]){
                callback.onPageLoad(null!=response&&response.isSucceed(),null!=response?response.getData():null);
            }
        });
        return ()->canceled[0]=true;
    }

    public final ObservableField<Long> getCurrentSelectSize() {
        return mCurrentSelectSize;
    }

    public BrowserListAdapter setOnPathSpanClick(PathSpanClick.OnPathSpanClick listener){
        mPathSpanClick.setOnClickListener(listener);
        return this;
    }

    @Override
    protected void onPageLoadFinish(boolean succeed, Page<File> page) {
        super.onPageLoadFinish(succeed, page);
        if (succeed){
            if (null!=page&&page instanceof Folder){
                Folder folder=(Folder)page;
                mCurrentFolder.set(folder);
                mCurrentPath.set(mPathSpanClick.generate(folder));
                if (getSize()>0&&folder.isEmpty()){
//                    toast(R.string.noMoreData,500);
                }
            }
        }
    }

    @Override
    protected ViewGroup.LayoutParams onCreateViewHolderLayoutParams(ViewGroup parent, int viewType, RecyclerView.ViewHolder viewHolder) {
        if (viewType==VIEW_TYPE_DATA){
            ViewGroup.MarginLayoutParams params=new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin=params.topMargin=5;
            return params;
        }
        return super.onCreateViewHolderLayoutParams(parent, viewType, viewHolder);
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

    public boolean removeIfInFolder(File file){
        return null!=file&&isCurrentFolder(file.getPath())&&remove(file);
    }

    public boolean isCurrentFolder(File file){
        return null!=file&&isCurrentFolder(file.getPath());
    }

    public boolean isCurrentFolder(String path){
        if (null==path||path.length()<=0){
            return false;
        }
        BrowseQuery current=getCurrent();
        File file=null!=current?current.mFolder:null;
        String folderPath=null!=file?file.getPath():null;
        return null!=folderPath&&path.startsWith(folderPath);
    }

    public boolean setClient(Client client){
        Client current=getClient();
        if ((null==current&&null==client)||(null!=current&&null!=client&&current==client)){
            return false;
        }
        mCurrentPath.set(null);
        mCurrentFolder.set(null);
        mBrowserClient.set(client);
        cleanArgs();
        clean();
        return reset(null);
    }

    public Client getClient(){
        return mBrowserClient.get();
    }

    public ObservableField<Client> getBrowserClient() {
        return mBrowserClient;
    }

    public final boolean setMode(Mode mode){
        mMode=mode;
        notifyAttachedItemChanged();
        return true;
    }

    public final boolean selectFile(File file){
        Mode current=mMode;
        int index= null!=file&&null!=current?indexPosition(file):-1;
        if (index>=0){
            current.add(file);
            notifyItemChanged(index,"SelectFile.");
            return true;
        }
        return false;
    }

    public final boolean unSelectFile(File file){
        Mode current=mMode;
        int index= null!=file&&null!=current?indexPosition(file):-1;
        if (index>=0){
            current.remove(file);
            notifyItemChanged(index,"UnselectFile.");
            return true;
        }
        return false;
    }

    public final boolean isSelectedFile(File file){
        Mode current=null!=file?mMode:null;
        return null!=current&&current.isContains(file);
    }

    private boolean cancelLoadThumb(ItemBrowserFileBinding binding){
        Map<RecyclerView.ViewHolder,Canceler> thumbLoading=mThumbLoading;
        Canceler canceler=null!=binding&&null!=thumbLoading?thumbLoading.get(binding):null;
        if (null!=canceler){
            canceler.cancel();
            thumbLoading.remove(canceler);
            return true;
        }
        return false;
    }

    private Canceler loadThumb(View view,Client client,File file,OnFinish<Drawable> callback){
        if (null==callback||null==client||null==file){
            return null;
        }
        final Canceled canceled=new Canceled();
        return execute(()->callback.onFinish(client.loadThumb(view,file,canceled)))?canceled:null;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        View itemView=null!=holder?holder.itemView:null;
        ViewDataBinding binding=null!=itemView? DataBindingUtil.getBinding(itemView):null;
        if (null==binding||!(binding instanceof ItemBrowserFileBinding)){
            return;
        }
        cancelLoadThumb((ItemBrowserFileBinding)binding);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Collection<Canceler>  cancelers=mThumbLoading.values();
        if (null!=cancelers){
            for (Canceler canceler:cancelers) {
                canceler.cancel();
            }
            mThumbLoading.clear();
        }
    }

    @Override
    protected void onPageLoadingChange(boolean loading) {
        super.onPageLoadingChange(loading);
        mPageLoading.set(loading);
    }

    @Override
    protected Object onCreateViewTypeHolder(int viewType, ViewGroup parent) {
        return viewType==VIEW_TYPE_DATA? R.layout.item_browser_file:
                super.onCreateViewTypeHolder(viewType,parent);
    }

    @Override
    protected void onBindData(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        View itemView=null!=holder?holder.itemView:null;
        ViewDataBinding binding=null!=itemView? DataBindingUtil.getBinding(itemView):null;
        if (null!=binding&&binding instanceof ItemBrowserFileBinding){
            ItemBrowserFileBinding fileBinding=((ItemBrowserFileBinding)binding);
            cancelLoadThumb(fileBinding);
            File file=getItem(position);
            fileBinding.setThumb(itemView.getResources().getDrawable(BrowserBinding.instance().getThumbResId(file)));
            fileBinding.setPath(file);
            fileBinding.setPosition(position+1);
            Mode mode=mMode;
            fileBinding.setMode(mode);
            fileBinding.setSelected(null!=mode&&null!=file&&(mode.isAllEnabled()||mode.isContains(file)));
            fileBinding.setClickBinding(new ViewBinding(file));
            Client client=null;Canceler canceler;
            if (null!=file&&null!=(client=getClient())){//Try load file thumb
                Map<RecyclerView.ViewHolder,Canceler> thumbLoading=mThumbLoading;
                if (null!=(canceler=loadThumb(itemView,client,file,(Drawable thumb)-> postIfPossible(()->{
                    File currentFile=fileBinding.getPath();
                    if (null!=currentFile&&currentFile==file){
                        thumbLoading.remove(fileBinding);
                        if(null!=thumb){
                            fileBinding.setThumb(thumb);
                        }
                    }},0)))){
                    thumbLoading.put(holder,()->{
                        thumbLoading.remove(fileBinding);
                        return canceler.cancel();
                    });
                }
            }
       }
    }

    public Folder getFolder() {
        ObservableField<Folder> folderField=mCurrentFolder;
        return null!=folderField?folderField.get():null;
    }

    public ObservableField<Boolean> getPageLoading() {
        return mPageLoading;
    }

    public ObservableField<Folder> getCurrentFolder() {
        return mCurrentFolder;
    }

    public ObservableField<CharSequence> getCurrentPath() {
        return mCurrentPath;
    }
}
