package com.luckmerlin.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangeUpdater extends MatcherInvoker implements ChangeUpdate{
    private transient List<OnChangeUpdate> mListeners;

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
        return match(mListeners,matcher);
    }
}
