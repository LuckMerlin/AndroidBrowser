package com.luckmerlin.binding;

import android.view.View;

public interface ImageFetcher {

    interface OnImageFetch{
        void onImageFetched(Object image);
    }
    void fetchImage(View view,Binding binding,OnImageFetch callback);
}
