package com.luckmerlin.plugin;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.object.ObjectCreator;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.ContentResolver;
import java.lang.reflect.Field;

public abstract class PluginContext extends ContextWrapper {
    private Resources mPluginResources;
    private ClassLoader mPluginClassLoader;

    public PluginContext(Context base) {
        super(base);
    }

    protected abstract AssetManager onCreateAssetManager();

    public abstract PackageInfo getPackageInfo(int flags);

    @Override
    public final Resources getResources() {
        if(null==mPluginResources){
            try {
                AssetManager assetManager=onCreateAssetManager();
                Context base=getBaseContext();
                Resources resources=null!=base?base.getResources():null;
                mPluginResources= null!=resources&&null!=assetManager?new Resources(assetManager,
                        resources.getDisplayMetrics(), resources.getConfiguration()):mPluginResources;
            }catch (Exception e){
                //Do nothing
            }
        }
        return null!=mPluginResources?mPluginResources:super.getResources();
    }

    @Override
    public final ClassLoader getClassLoader() {
        if (null==mPluginClassLoader){
            ClassLoader classLoader=onCreateClassLoader();
            if (null!=classLoader){
                wrapperClassLoader(classLoader,PluginContext.class.getClassLoader());
                mPluginClassLoader=classLoader;
            }
        }
        return super.getClassLoader();
    }

    public final Object sss(){
        ActivityInfo[] activities=getActivities();
        if (null!=activities){
            String activityName=null;Class cls=null;
            for (ActivityInfo info:activities) {
                if (null!=(activityName=info.name)&&activityName.length()>0){
//                    info.
                    Debug.D("DDDDDD明细 "+info.name);
                }
            }
        }
        return null;
    }

    public final Content createPluginContent(Class contentClass){
        Class cls=null!=contentClass?loadPluginClass(contentClass.getName()):null;
        if (null!=cls&&ContentResolver.class.isAssignableFrom(cls)){
            Debug.D("AAAAA "+cls);
//            return ((ContentResolver)new ObjectCreator().createObject(cls)).onResolveContent(this);
        }
        return null;
    }

    public final Class loadPluginClass(String clsName){
        ClassLoader classLoader=null!=clsName&&clsName.length()>0?getClassLoader():null;
        try {
            return null!=classLoader?classLoader.loadClass(clsName):null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static final boolean wrapperClassLoader(ClassLoader from,ClassLoader to){
        if(null==from||null==to){
            return false;
        }
        ClassLoader toParent=to.getParent();
        try {
            Field field=ClassLoader.class.getDeclaredField("parent");
            field.setAccessible(true);
            field.set(from,toParent);
            field.set(to,from);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected abstract ClassLoader onCreateClassLoader();

    public final ActivityInfo[] getActivities(){
        PackageInfo packageInfo=getPackageInfo(PackageManager.GET_ACTIVITIES);
        return null!=packageInfo?packageInfo.activities:null;
    }
}
