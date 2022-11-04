package com.luckmerlin.plugin;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import com.luckmerlin.debug.Debug;
import dalvik.system.DexClassLoader;

public class PluginFileContext extends PluginContext {
    private String mApkPath;

    public PluginFileContext(Context context, String apkPath) {
        super(context);
        mApkPath=apkPath;
    }

    public final PluginFileContext setPluginApkPath(String apkPath){
        mApkPath=apkPath;
        return this;
    }

    @Override
    protected AssetManager onCreateAssetManager() {
        try {
            String apkPath=mApkPath;
            if (null==apkPath||apkPath.length()<=0){
                return null;
            }
            AssetManager assetManager = AssetManager.class.newInstance();
            AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(assetManager, apkPath);
            return assetManager;
        }catch (Throwable e) {
            Debug.E("Exception create plugin context assert manager.e="+e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected ClassLoader onCreateClassLoader() {
        String apkPath=mApkPath;
        if (null!=apkPath&&apkPath.length()>0){
            return new DexClassLoader(apkPath,getCacheDir().
                    getAbsolutePath(), null,getClass().getClassLoader());
        }
        return null;
    }

    @Override
    public final PackageInfo getPackageInfo(int flags) {
        String apkPath=mApkPath;
        if (null==apkPath||apkPath.length()<=0){
            return null;
        }
        PackageManager manager=getPackageManager();
        return null!=manager?manager.getPackageArchiveInfo(apkPath,flags):null;
    }

}
