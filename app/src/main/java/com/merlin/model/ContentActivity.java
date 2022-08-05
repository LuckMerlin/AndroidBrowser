package com.merlin.model;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.ContentResolver;
import com.luckmerlin.view.ViewIterate;

public abstract class ContentActivity extends Activity implements ContentResolver {
    private Content mModel=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Content model=mModel=this instanceof ContentResolver ?((ContentResolver)this).onResolveContent():null;
        if (null!=model){
            View contentView= model.onCreateContentView(this,(ViewIterate iterate)->
                    null!=iterate&&((null!=model&&iterate.iterate(model))));
            if (null!=contentView){
                setContentView(contentView);
            }
            if (model instanceof OnActivityCreate){
                ((OnActivityCreate)model).onCreate(savedInstanceState,this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Content model=mModel;
        if (null!=model&&model instanceof OnActivityDestroy){
            ((OnActivityDestroy)model).onDestroy(this);
        }
    }

    @Override
    public void onBackPressed() {
        Content model=mModel;
        if (null!=model&&model instanceof OnBackPress&&((OnBackPress)model).onBackPressed()){
            return;
        }
        super.onBackPressed();
    }

    public final Content getContent() {
        return mModel;
    }
}
