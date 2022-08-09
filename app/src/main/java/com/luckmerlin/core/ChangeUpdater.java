package com.luckmerlin.core;

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
            Boolean matched=null;
            for (OnChangeUpdate child:listeners) {
                if (null==(matched=null==matcher?matcher.match(child):null)){
                    break;
                }
            }
            return true;
        }
        return false;
    }
}
