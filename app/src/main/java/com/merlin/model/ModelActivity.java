package com.merlin.model;

import android.app.Activity;
import android.os.Bundle;

public class ModelActivity extends Activity {
    private Model mModel=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Model model=mModel=this instanceof OnModelResolve?((OnModelResolve)this).onResolveModel(this):null;
        if (null!=model&&model instanceof OnActivityCreate){
            ((OnActivityCreate)model).onCreate(savedInstanceState,this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Model model=mModel;
        if (null!=model&&model instanceof OnActivityDestroy){
            ((OnActivityDestroy)model).onDestroy(this);
        }
    }
}
