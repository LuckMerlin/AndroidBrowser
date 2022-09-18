package com.merlin.adapter;

import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.luckmerlin.core.Canceler;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.Headers;

import java.util.HashMap;
import java.util.List;

public class PageListAdapter<A,T> extends ListAdapter<T> implements SwipeRefreshLayout.OnRefreshListener {
    private boolean mEmptyReset=true;
    private int mPageSize;
    private A mArgs;
    private LoadingPage<A,T> mLoadingPage;
    private PageLoader<A,T> mPageLoader;
    private OnPageLoadListener<T> mOnPageLoadedListener;
    private SwipeRefreshLayout mRefreshLayout;
    private final RecyclerView.OnScrollListener mOnScrollListener=new RecyclerView.OnScrollListener(){
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState){
                case RecyclerView.SCROLL_STATE_IDLE:
                    SwipeRefreshLayout refreshLayout=mRefreshLayout;
                    if (null!=refreshLayout&&refreshLayout.isRefreshing()){
                        return;
                    }
                    RecyclerView.LayoutManager manager=null!=recyclerView?recyclerView.getLayoutManager():null;
                    if (null!=manager&&manager instanceof LinearLayoutManager){
                        LinearLayoutManager layoutManager=(LinearLayoutManager)manager;
                        if (layoutManager.findLastVisibleItemPosition()>=getSize()-1){
                            loadNext(mPageSize,null);
                        }
                    }
                    break;
            }
        }
    };

    protected PageListAdapter() {
        this(new ItemCallback<>());
    }

    protected PageListAdapter(DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    protected PageListAdapter(AsyncDifferConfig<T> config) {
        super(config);
    }

    public final PageListAdapter<A,T> setOnPageLoadedListener(OnPageLoadListener<T> listener) {
        this.mOnPageLoadedListener = listener;
        return this;
    }

    public final PageListAdapter<A,T> setPageLoader(PageLoader<A,T> pageLoader) {
        this.mPageLoader = pageLoader;
        return this;
    }

    public final PageListAdapter<A,T> setPageSize(int pageSize){
        mPageSize=pageSize;
        return this;
    }

    @Override
    public void onRefresh() {
        SwipeRefreshLayout refreshLayout=mRefreshLayout;
        reset(mArgs,null!=refreshLayout?(boolean succeed, Page<T> page)->
                refreshLayout.setRefreshing(false):null);
    }

    protected Canceler onPageLoad(A args,int fromIndex, T from, int pageSize, OnPageLoadListener<T> callback){
        PageLoader pageLoader=mPageLoader;
        return null!=pageLoader?pageLoader.onPageLoad(args,fromIndex,from,pageSize,callback):null;
    }

    public final boolean reset(OnPageLoadListener<T> callback){
        return reset(mArgs,callback);
    }

    public final boolean reset(A args,OnPageLoadListener<T> callback){
        return reset(args,mPageSize,callback);
    }

    public final int getPageSize() {
        return mPageSize;
    }

    public final boolean cleanArgs(){
        A arg=mArgs;
        mArgs=null;
        return null!=arg;
    }

    public final boolean reset(A args, int pageSize, OnPageLoadListener<T> callback){
        mLoadingPage=null;
        mArgs=null!=args?args:mArgs;
        return load(0,null, pageSize, (boolean succeed, Page<T> page)-> {
                if (succeed){
                    setData(null!=page?page.getPageData():null);
                }
                if (null!=callback){
                    callback.onPageLoad(succeed,page);
                }
        });
    }

    public final A getCurrent() {
        return mArgs;
    }

    public final boolean loadPre(int pageSize, OnPageLoadListener<T> callback){
        return load(0,getItem(0),pageSize==0?10:pageSize>0?-pageSize:pageSize,(boolean succeed, Page<T> page)-> {
            if (succeed){
                addAll(0,null!=page?page.getPageData():null);
            }
            if (null!=callback){
                callback.onPageLoad(succeed,page);
            }
        });
    }

    public final boolean loadNext(int pageSize,OnPageLoadListener<T> callback){
        int index=getSize()-1;
        return load(index<0?index:index+1,getItem(index), pageSize,(boolean succeed, Page<T> page)-> {
            if (succeed){
                addAll(Integer.MAX_VALUE,null!=page?page.getPageData():null);
            }
            if (null!=callback){
                callback.onPageLoad(succeed,page);
            }
        });
    }

    protected void onPageLoadingChange(boolean loading){
        //Do nothing
    }

    protected void onPageLoadFinish(boolean succeed,Page<T> page){
        //Do nothing
    }

    private boolean load(int fromIndex,T from,int pageSize,OnPageLoadListener<T> callback){
        pageSize=pageSize==0?10:pageSize > 0 ? pageSize : -pageSize;
        LoadingPage<A,T> loadingPage=mLoadingPage;
        if (null!=loadingPage){
            if (null!=callback){
                callback.onPageLoad(false,null);
            }
            return false;
        }
        A args=mArgs;
        onPageLoadingChange(true);
        loadingPage=mLoadingPage=new LoadingPage<A,T>(args,from,pageSize,callback){
            @Override
            public void onPageLoad(boolean succeed, Page page) {
                if (!isUiThread()){
                    postIfPossible(()->onPageLoad(succeed,page),0);
                    return;
                }
                boolean currentLoadFinish=false;
                LoadingPage<A,T> loadingPage=mLoadingPage;
                if (null!=loadingPage&&loadingPage==this){
                    currentLoadFinish=true;
                    mLoadingPage=null;
                    onPageLoadingChange(false);
                }
                super.onPageLoad(succeed, page);
                PageListAdapter.this.onPageLoadFinish(succeed,page);
                OnPageLoadListener<T> onPageLoad=mOnPageLoadedListener;
                if (currentLoadFinish&&null!=onPageLoad){
                    onPageLoad.onPageLoad(succeed,page);
                }
            }
        };
        loadingPage.canceler(onPageLoad(args,fromIndex,from,pageSize,loadingPage));
        if (null==loadingPage.mCanceler){//No canceler,means fail
            loadingPage.onPageLoad(false,null);
            return false;
        }
        return true;
    }

    public final boolean autoReset(boolean enable){
        if (mEmptyReset!=enable){
            mEmptyReset=enable;
            return true;
        }
        return false;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (null!=recyclerView&&(mEmptyReset&&getSize()<=0)){
            reset(null,mPageSize,null);
            ViewParent parent=recyclerView.getParent();
            if (null!=parent&&parent instanceof SwipeRefreshLayout){
                SwipeRefreshLayout refreshLayout=mRefreshLayout=(SwipeRefreshLayout)parent;
                refreshLayout.setOnRefreshListener(this);
            }
            recyclerView.addOnScrollListener(mOnScrollListener);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (null!=recyclerView){
            recyclerView.removeOnScrollListener(mOnScrollListener);
        }
        SwipeRefreshLayout refreshLayout=mRefreshLayout;
        if (null!=refreshLayout){
            mRefreshLayout=null;
            refreshLayout.setOnRefreshListener(null);
        }
    }

    public static interface Page<T>{
        List<T> getPageData();
    }

    public interface OnPageLoadListener<T>{
        void onPageLoad(boolean succeed,Page<T> page);
    }

    private static class LoadingPage<A,T> implements OnPageLoadListener<T>,Canceler{
        private final A mArgs;
        private final T mFrom;
        private final int mPageSize;
        private final OnPageLoadListener<T> mCallback;
        private Canceler mCanceler;
        private boolean mCanceled=false;

        protected LoadingPage(A args,T from,int pageSize,OnPageLoadListener<T> callback){
            mArgs=args;
            mFrom=from;
            mPageSize=pageSize;
            mCallback=callback;
        }

        @Override
        public void onPageLoad(boolean succeed,Page<T> page) {
            OnPageLoadListener<T> callback=mCallback;
            if (null!=callback){
                callback.onPageLoad(succeed,page);
            }
        }

        public LoadingPage<A,T> canceler(Canceler canceler){
            mCanceler=canceler;
            if (mCanceled&&null!=canceler){
                canceler.cancel();
            }
            return this;
        }

        @Override
        public boolean cancel() {
            mCanceled=true;
            Canceler canceler=mCanceler;
            if (null!=canceler){
                return canceler.cancel();
            }
            return false;
        }
    }

    public interface PageLoader<A,T>{
        Canceler onPageLoad(A args,int fromIndex, T from, int pageSize, OnPageLoadListener<T> callback);
    }
}
