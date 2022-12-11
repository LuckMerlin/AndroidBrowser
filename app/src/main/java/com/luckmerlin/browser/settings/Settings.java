package com.luckmerlin.browser.settings;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.databinding.ObservableArrayMap;

import com.luckmerlin.browser.ClientMeta;
import com.luckmerlin.browser.client.Client;
import com.luckmerlin.json.JsonObject;
import org.json.JSONObject;
import java.util.Map;
import java.util.Set;

public class Settings extends ObservableArrayMap<String,Object> {
    private volatile static Settings mInstance;
    private static final String LABEL_CLIENT_BROWSE_PATH="clientBrowsePath";
    private static final String LABEL_SAVE_LATEST_BROWSE_PATH="browserLatestPathEnable";
    private static final String SAVE_NAME="BrowserSetting.settings";
    private boolean mLoaded=false;
    private String mLatestChangedKey;

    private Settings () {
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

    public static Settings Instance(){
        return I();
    }

    public boolean load(Context context,boolean force){
        if (mLoaded&&!force){
            return false;
        }
        SharedPreferences preferences=null!=context?context.getSharedPreferences(SAVE_NAME, Context.MODE_PRIVATE):null;
        if (null!=preferences){
            Map<String,?> map=preferences.getAll();
            mLoaded=true;
            if (null!=map){
                putAll(map);
            }
            return true;
        }
        return false;
    }

    public boolean save(Context context,String...keys){
        SharedPreferences preferences=null!=context?context.getSharedPreferences(SAVE_NAME, Context.MODE_PRIVATE):null;
        SharedPreferences.Editor editor=null!=preferences?preferences.edit():null;
        if (null==editor){
            return false;
        }else if (null!=keys&&keys.length>0){
            for (String child:keys) {
                save(editor,child,get(child));
            }
            return true;
        }
        Set<String> set=keySet();
        if (null==set||set.size()<=0){
            editor.clear().commit();
            return true;
        }
        for (String child:set) {
            save(editor,child,get(child));
        }
        return true;
    }

    private boolean save(SharedPreferences.Editor editor,String key,Object value){
        if (null==editor||null==key){
            return false;
        }else if (null==value){
            return editor.remove(key).commit();
        }else if (value instanceof String){
            return editor.putString(key,(String)value).commit();
        }else if (value instanceof Integer){
            return editor.putInt(key,(Integer)value).commit();
        }else if (value instanceof Long){
            return editor.putLong(key,(Long)value).commit();
        }else if (value instanceof Boolean){
            return editor.putBoolean(key,(Boolean) value).commit();
        }else if (value instanceof Float){
            return editor.putFloat(key,(Float) value).commit();
        }
        return save(editor,key,value.toString());
    }

    private String getClientHost(Client client){
        ClientMeta meta=null!=client?client.getMeta():null;
        String host=null!=meta?meta.getHost():null;
        return null!=host?host:"";
    }

    public boolean insertClientBrowserPath(Client client, String path){
        if (!isBrowserLastPathEnable()){
            return false;
        }
        String host=null!=path&&path.length()>0?getClientHost(client):null;
        if (null==host){
            return false;
        }
        JSONObject clientPaths=getJsonObject(LABEL_CLIENT_BROWSE_PATH,null);
        clientPaths=null!=clientPaths?clientPaths:new JSONObject();
        JsonObject.putSafe(clientPaths,host,path);
        put(LABEL_CLIENT_BROWSE_PATH,clientPaths,true);
        return true;
    }

    public String getClientLatestBrowserPath(Client client){
        String host=null!=client?getClientHost(client):null;
        JSONObject clientPaths=null!=host?getJsonObject(LABEL_CLIENT_BROWSE_PATH,null):null;
        return null!=clientPaths?clientPaths.optString(host):null;
    }

    public final Settings enableSaveLatestBrowserPath(boolean enable){
        put(LABEL_SAVE_LATEST_BROWSE_PATH,enable);
        return this;
    }

    public boolean saveLatestChanged(Context context){
        String key=mLatestChangedKey;
        if (null!=context&&null!=key){
            mLatestChangedKey=null;
            return save(context,key);
        }
        return false;
    }

    public final boolean isBrowserLastPathEnable() {
        return getBoolean(LABEL_SAVE_LATEST_BROWSE_PATH,false);
    }

    public final String getString(String key,String def){
        Object value=get(key);
        return null!=value&&value instanceof String?(String)value:def;
    }

    public final boolean getBoolean(String key,boolean def){
        Object value=get(key);
        return null!=value&&value instanceof Boolean?(Boolean)value:def;
    }

    public final JSONObject getJsonObject(String key,JsonObject def){
        JSONObject json= JsonObject.makeJson(get(key));
        return null!=json?json:def;
    }

    @Override
    public Object put(String k, Object v) {
        return put(k,v,false);
    }

    private Object put(String k, Object v,boolean changed) {
        Object current=get(k);
        if (!changed&&((null==v&&null==current)||(null!=v&&null!=current&&v.equals(current)))){
            return current;
        }
        mLatestChangedKey=k;
        return super.put(k, v);
    }
}
