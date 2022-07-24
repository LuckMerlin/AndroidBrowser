package com.merlin.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.luckmerlin.core.Canceler;
import com.luckmerlin.http.Headers;

import java.util.HashMap;
import java.util.List;

public class PageListAdapter<A,T> extends ListAdapter<T> {
    private boolean mEmptyReset=true;
    private int mPageSize;
    private A mArgs;
    private LoadingPage<A,T> mLoadingPage;
    private PageLoader<A,T> mPageLoader;
    private OnPageLoadListener<T> mOnPageLoadedListener;

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

    protected Canceler onPageLoad(A args, T from, int pageSize, OnPageLoadListener<T> callback){
        PageLoader pageLoader=mPageLoader;
        return null!=pageLoader?pageLoader.onPageLoad(args,from,pageSize,callback):null;
    }

    public final boolean reset(A args,OnPageLoadListener<T> callback){
        return reset(args,mPageSize,callback);
    }

    public final boolean reset(A args,int pageSize,OnPageLoadListener<T> callback){
        clean();
        mArgs=null!=args?args:mArgs;
        return loadNext(pageSize,callback);
    }

    public final A getCurrent() {
        return mArgs;
    }

    public final boolean loadPre(int pageSize, OnPageLoadListener<T> callback){
        return load(getFirst(),pageSize==0?10:pageSize>0?-pageSize:pageSize,(boolean succeed, Page<T> page)-> {
            if (succeed){
                addAll(0,null!=page?page.getPageData():null);
            }
            if (null!=callback){
                callback.onPageLoad(succeed,page);
            }
        });
    }

    public final boolean loadNext(int pageSize,OnPageLoadListener<T> callback){
        return load(getLatest(), pageSize==0?10:pageSize > 0 ? pageSize : -pageSize,(boolean succeed, Page<T> page)-> {
            if (succeed){
                addAll(Integer.MAX_VALUE,null!=page?page.getPageData():null);
            }
            if (null!=callback){
                callback.onPageLoad(succeed,page);
            }
        });
    }

    private boolean load(T from,int pageSize,OnPageLoadListener<T> callback){
        LoadingPage<A,T> loadingPage=mLoadingPage;
        if (null!=loadingPage){
            if (null!=callback){
                callback.onPageLoad(false,null);
            }
            return false;
        }
        A args=mArgs;
        loadingPage=mLoadingPage=new LoadingPage<A,T>(args,from,pageSize,callback){
            @Override
            public void onPageLoad(boolean succeed, Page page) {
                boolean currentLoadFinish=false;
                LoadingPage<A,T> loadingPage=mLoadingPage;
                if (null!=loadingPage&&loadingPage==this){
                    currentLoadFinish=true;
                    mLoadingPage=null;
                }
                super.onPageLoad(succeed, page);
                OnPageLoadListener<T> onPageLoad=mOnPageLoadedListener;
                if (currentLoadFinish&&null!=onPageLoad){
                    onPageLoad.onPageLoad(succeed,page);
                }
            }
        };
        loadingPage.canceler(onPageLoad(args,from,pageSize,loadingPage));
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
        Canceler onPageLoad(A args, T from, int pageSize, OnPageLoadListener<T> callback);
    }
}
