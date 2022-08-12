package com.luckmerlin.core;

import java.util.Collection;
import java.util.Iterator;

public class MatcherInvoker {

    public <T> boolean match(T[] array,Matcher<T> matcher){
        int length=null!=array?array.length:0;
        if (length<=0){
            return false;
        }
        matcher=null!=matcher?matcher:(T data)-> false;
        for (int i = 0; i < length; i++) {
            if (!matcher.match(array[i])){
                break;
            }
        }
        return true;
    }

    public <T> boolean match(Collection<T> collection, Matcher<T> matcher){
        return null!=collection&&match(collection.iterator(),matcher);
    }

    public <T> boolean match(Iterator<T> iterator, Matcher<T> matcher){
        if (null==iterator){
            return false;
        }
        matcher=null!=matcher?matcher:(T data)-> false;
        while (iterator.hasNext()){
            if (!matcher.match(iterator.next())){
                break;
            }
        }
        return true;
    }
}
