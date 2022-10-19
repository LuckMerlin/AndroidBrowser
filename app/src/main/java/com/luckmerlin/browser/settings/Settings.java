package com.luckmerlin.browser.settings;

import android.content.Context;
import com.luckmerlin.browser.Client;
import com.luckmerlin.json.JsonObject;

public class Settings extends JsonObject {
    private volatile static Settings mInstance;

    private Settings () {}

    public static Settings getInstance() {
        if (mInstance == null) {
            synchronized (Settings.class) {
                if (mInstance == null) {
                    mInstance = new Settings();
                }
            }
        }
        return mInstance;
    }

    public boolean load(Context context){

        return false;
    }

    public boolean save(Context context){

        return false;
    }

    public boolean insertClientBrowserPath(Client client, String path){
        return false;
    }

    public String getClientLatestBrowserPath(Client client){
        return null;
    }

}
