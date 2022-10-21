package com.luckmerlin.browser.settings;

import android.os.Environment;
import androidx.databinding.ObservableField;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.ClientMeta;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.json.OnJsonUpdate;
import com.luckmerlin.utils.Utils;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Settings extends JsonObject implements OnJsonUpdate {
    private volatile static Settings mInstance;
    private static final File mSaveFile=new File(Environment.getExternalStorageDirectory(),".Settings");
    private static final String LABEL_CLIENT_BROWSE_PATH="clientBrowsePath";
    private static final String LABEL_SAVE_LATEST_BROWSE_PATH="browserLatestPathEnable";
    private final ObservableField<Settings> mSettings=new ObservableField<>();

    private Settings () {
        super(readSettings(mSaveFile));
        mSettings.set(this);
    }

    public static Settings Instance() {
        if (mInstance == null) {
            synchronized (Settings.class) {
                if (mInstance == null) {
                    mInstance = new Settings();
                }
            }
        }
        return mInstance;
    }

    public static ObservableField<Settings> I(){
        return Instance().mSettings;
    }

    public boolean save(){
        File saveFile=mSaveFile;
        if (null==saveFile){
            Debug.W("Can't save browser settings while save file invalid.");
            return false;
        }
        FileOutputStream outputStream=null;
        try {
            String settingsJson=toString();
            if (null==settingsJson){
                return saveFile.exists()&&saveFile.delete();
            }
            if (!saveFile.exists()){
                saveFile.createNewFile();
            }
            outputStream=new FileOutputStream(saveFile);
            outputStream.write(settingsJson.getBytes());
            outputStream.flush();
            Debug.D("保存 "+settingsJson);
            return true;
        } catch (IOException e) {
            Debug.W("Exception save browser settings.e="+e);
            e.printStackTrace();
        }finally {
            Utils.closeStream(outputStream);
        }
        return false;
    }

    private String getClientHost(Client client){
        ClientMeta meta=null!=client?client.getMeta():null;
        String host=null!=meta?meta.getHost():null;
        return null!=host?host:"";
    }

    @Override
    public boolean onChangeUpdated(String newData) {
        if (null!=mSettings){
            mSettings.notifyChange();
        }
        return true;
    }

    public boolean insertClientBrowserPath(Client client, String path){
        String host=null!=path&&path.length()>0?getClientHost(client):null;
        if (null==host){
            return false;
        }
        JSONObject clientPaths=optJSONObject(LABEL_CLIENT_BROWSE_PATH);
        clientPaths=null!=clientPaths?clientPaths:new JSONObject();
        putSafe(clientPaths,host,path);
        putSafe(this,LABEL_CLIENT_BROWSE_PATH,clientPaths);
        save();
        return true;
    }

    public String getClientLatestBrowserPath(Client client){
        String host=null!=client?getClientHost(client):null;
        JSONObject clientPaths=null!=host?optJSONObject(LABEL_CLIENT_BROWSE_PATH):null;
        return null!=clientPaths?clientPaths.optString(host):null;
    }

    public final Settings enableSaveLatestBrowserPath(boolean enable){
        putSafe(this,LABEL_SAVE_LATEST_BROWSE_PATH,enable);
        save();
        return this;
    }

    public final boolean isBrowserLastPathEnable() {
        return optBoolean(LABEL_SAVE_LATEST_BROWSE_PATH,false);
    }

    private static String readSettings(File saveFile){
        BufferedReader reader=null;
        try {
            reader=null!=saveFile&&saveFile.exists()?new BufferedReader(new FileReader(saveFile)):null;
            if (null!=reader){
                StringBuilder builder=new StringBuilder();
                String line=null;
                while (null!=(line=reader.readLine())){
                    builder.append(line);
                }
                return builder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Utils.closeStream(reader);
        }
        return null;
    }
}
