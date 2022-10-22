package com.luckmerlin.core;

import java.util.Collection;
import java.util.Iterator;

public class MatcherInvoker {

    public <T> boolean match(T[] array,Matcher<T> matcher){
        int length=null!=array?array.length:0;
        if (length<=0||null==matcher){
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (null==matcher.match(array[i])){
                break;
            }
        }
        return true;
    }

    public <T> boolean match(Collection<T> collection, Matcher<T> matcher){
        return null!=collection&&match(collection.iterator(),matcher);
    }

    public <T> boolean match(Iterator<T> iterator, Matcher<T> matcher){
        if (null==iterator||null==matcher){
            return false;
        }
        while (iterator.hasNext()){
            if (null==matcher.match(iterator.next())){
                break;
            }
        }
        return true;
    }
}
