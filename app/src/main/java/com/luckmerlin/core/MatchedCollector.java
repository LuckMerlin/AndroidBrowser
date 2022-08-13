package com.luckmerlin.core;

import java.util.ArrayList;
import java.util.List;

public class MatchedCollector<T> implements Matcher<T>{
    private final int mLimit;
    private Matcher<T> mMatcher;
    private List<T> mMatched;

    public MatchedCollector(int limit){
        mLimit=limit;
    }

    public final MatchedCollector<T> setMatcher(Matcher<T> matcher) {
        this.mMatcher = matcher;
        return this;
    }

    protected Boolean onMatch(T data){
        Matcher<T> matcher=mMatcher;
        return null!=matcher?matcher.match(data):null;
    }

    @Override
    public final Boolean match(T data) {
        if (mLimit<=0){
            return null;
        }
        Boolean matchedBool=null!=data? onMatch(data):false;
        if (null!=matchedBool&&matchedBool){
            List<T> matched=mMatched;
            (matched=null!=matched?matched:(mMatched=new ArrayList<>())).add(data);
            if (matched.size()>=mLimit){
                return null;
            }
        }
        return matchedBool;
    }

    public final List<T> getMatched(){
        return mMatched;
    }

    public final T getFirstMatched(){
        List<T> matched=mMatched;
        return null!=matched&&matched.size()>0?matched.get(0):null;
    }
}
