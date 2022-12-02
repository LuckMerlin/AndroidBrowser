package com.luckmerlin.plugin;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import com.luckmerlin.debug.Debug;

public class PluginApkContext extends PluginContext {
    private String mPackageName;
    private Context mPluginContext;

    public PluginApkContext(Context base, String packageName) {
        super(base);
        mPackageName=packageName;
    }

    public final PluginApkContext setPluginPackageName(String packageName) {
        this.mPackageName = packageName;
        mPluginContext=null;
        return this;
    }

    private Context getPluginContext(){
        try {
            if (null==mPluginContext){
                Context base=getBaseContext();
                String packageName=mPackageName;
                if (null!=base&&null!=packageName&&packageName.length()>0){
                   mPluginContext=base.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Debug.E("Exception create apk plugin context.e="+e);
//            e.printStackTrace();
        }
        return null!=mPluginContext?mPluginContext:super.getBaseContext();
    }

    @Override
    protected AssetManager onCreateAssetManager() {
        Context pluginContext=getPluginContext();
        return null!=pluginContext?pluginContext.getAssets():null;
    }

    @Override
    public PackageInfo getPackageInfo(int flags) {
        String packageName=mPackageName;
        if (null==packageName||packageName.length()<=0){
            return null;
        }
        PackageManager manager=getPackageManager();
        try {
            return null!=manager?manager.getPackageInfo(packageName,flags):null;
        } catch (PackageManager.NameNotFoundException e) {
            Debug.E("Exception get apk plugin package info.e="+e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected ClassLoader onCreateClassLoader() {
        Context pluginContext=getPluginContext();
        return null!=pluginContext?pluginContext.getClassLoader():null;
    }
}
