package com.luckmerlin.browser.settings;

import android.os.Environment;
import androidx.databinding.ObservableField;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.ClientMeta;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Settings {
    private volatile static Settings mInstance;
    private boolean mLoaded=false;
    private final File mSaveFile;
    private JsonObject mSettings;
    private final ObservableField<Boolean> mBrowserLastPathEnable= new SettingField<>("BrowserLastPathEnable",false);
    private final ObservableField<JsonObject> mClientBrowsePaths=new SettingField<>("clientBrowsePaths",null);

    private Settings () {
        File saveFile=mSaveFile=new File(Environment.getExternalStorageDirectory(),".Settings");
        BufferedReader reader=null;
        try {
            reader=null!=saveFile&&saveFile.exists()?new BufferedReader(new FileReader(saveFile)):null;
            if (null!=reader){
                StringBuilder builder=new StringBuilder();
                String line=null;
                while (null!=(line=reader.readLine())){
                    builder.append(line);
                }
                mSettings=new JsonObject(builder.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Utils.closeStream(reader);
        }
    }

    public static Settings I() {
        if (mInstance == null) {
            synchronized (Settings.class) {
                if (mInstance == null) {
                    mInstance = new Settings();
                }
            }
        }
        return mInstance;
    }

    public boolean save(){
        File saveFile=mSaveFile;
        if (null==saveFile){
            Debug.W("Can't save browser settings while save file invalid.");
            return false;
        }
        FileOutputStream outputStream=null;
        try {
            JsonObject settings=mSettings;
            String settingsJson=null!=settings?settings.toString():null;
            if (null==settingsJson){
                return saveFile.exists()&&saveFile.delete();
            }
            if (!saveFile.exists()){
                saveFile.createNewFile();
            }
            outputStream=new FileOutputStream(saveFile);
            outputStream.write(settingsJson.getBytes());
            outputStream.flush();
            return true;
        } catch (IOException e) {
            Debug.W("Exception save browser settings.e="+e);
            e.printStackTrace();
        }finally {
            Utils.closeStream(outputStream);
        }
        return false;
    }

    public static <T> T getValue(ObservableField<T> field,T def){
        T value=null!=field?field.get():null;
        return null!=value?value:def;
    }

    public static <T> boolean setValue(ObservableField<T> field, T value){
        if (null!=field){
            field.set(value);
            return true;
        }
        return false;
    }

    public boolean insertClientBrowserPath(Client client, String path){
        ClientMeta meta=null;
        if (null==path||path.length()<=0||null==(meta=null!=client?client.getMeta():null)){
            return false;
        }
        String host=meta.getHost();
        host=null!=host?host:"";
        JsonObject array=mClientBrowsePaths.get();
        String currentValue=null;
        if (null==array){
            array=new JsonObject();
            array.putSafe(host,path);
            mClientBrowsePaths.set(array);
            return true;
        }else if(null!=(currentValue=array.optString(host,null))&&currentValue.equals(path)){
            return false;
        }
        array.putSafe(host,path);
        mClientBrowsePaths.notifyChange();
        return true;
    }

    public String getClientLatestBrowserPath(Client client){
        ClientMeta meta=null!=client?client.getMeta():null;
        if (null==meta){
            return null;
        }
        String host=meta.getHost();
        host=null!=host?host:"";
        JsonObject dictionary=mClientBrowsePaths.get();
        return null!=dictionary?dictionary.optString(host):null;
    }

    public final ObservableField<Boolean> isBrowserLastPathEnable() {
        return mBrowserLastPathEnable;
    }

    private static class SettingField<T> extends ObservableField<T>{
        private final String mKey;

        public SettingField(String key,T value) {
            super(value);
            mKey=key;
        }

        @Override
        public void notifyChange() {
            super.notifyChange();
            Settings settings=Settings.I();
            JsonObject jsonObject=settings.mSettings==null?new JsonObject():settings.mSettings;
            jsonObject.putSafe(mKey,get());
            settings.save();
        }
    }
}
