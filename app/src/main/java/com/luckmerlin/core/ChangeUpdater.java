package com.luckmerlin.core;

import com.luckmerlin.debug.Debug;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangeUpdater implements ChangeUpdate{
    private List<OnChangeUpdate> mListeners;

    @Override
    public boolean addChangeListener(OnChangeUpdate changeUpdate) {
        if (null==changeUpdate){
            return false;
        }
        List<OnChangeUpdate> listeners=mListeners;
        listeners=null!=listeners?listeners:(mListeners=new CopyOnWriteArrayList<>());
        return !listeners.contains(changeUpdate)&&listeners.add(changeUpdate);
    }

    @Override
    public boolean removeChangeListener(OnChangeUpdate changeUpdate) {
        List<OnChangeUpdate> listeners=null!=changeUpdate?mListeners:null;
        return null!=listeners&&listeners.remove(changeUpdate);
    }

    public final boolean iterateUpdaters(Matcher<OnChangeUpdate> matcher){
        List<OnChangeUpdate> listeners=mListeners;
        if (null!=listeners){
            for (OnChangeUpdate child:listeners) {
                if (null!=matcher&&null==matcher.match(child)){
                    break;
                }
            }
            return true;
        }
        return false;
    }
}