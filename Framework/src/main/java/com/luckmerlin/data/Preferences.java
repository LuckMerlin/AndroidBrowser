package com.luckmerlin.data;

import android.content.SharedPreferences;
import android.util.Base64;
import java.util.Map;
import java.util.Set;

public class Preferences {
    private final SharedPreferences mPreferences;

    public interface OnPreferencesLoad{
        void onPreferencesLoaded(String key,byte[] bytes);
    }

    public Preferences(SharedPreferences preferences){
        mPreferences=preferences;
    }

    public final boolean delete(String key){
        SharedPreferences preferences=mPreferences;
        if (null==key||null==preferences){
            return false;
        }
        return preferences.edit().remove(key).commit();
    }

    public final void load(OnPreferencesLoad callback){
        SharedPreferences preferences=mPreferences;
        Map<String,?> map=null!=callback&&null!=preferences?preferences.getAll():null;
        Set<String> set=null!=map?map.keySet():null;
        if (null!=set){
            Object value=null;byte[] bytes=null;
            for (String child:set) {
                if (null!=(value=null!=child?map.get(child):null)&& value instanceof String&&
                        null!=(bytes= Base64.decode((String)value, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING))){
                    callback.onPreferencesLoaded(child,bytes);
                }
            }
        }
    }

    public final boolean write(String key, byte[] taskBytes){
        SharedPreferences preferences=mPreferences;
        if (null==key||key.length()<=0||null==preferences||null==taskBytes||taskBytes.length<=0){
            return false;
        }
        return preferences.edit().putString(key,
                Base64.encodeToString(taskBytes,  Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING)).commit();
    }
}
