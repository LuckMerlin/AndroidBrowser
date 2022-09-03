package com.luckmerlin.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComparedList<T>{
    private List<T> mList;
    private final Comparator<T> mComparator;

    public ComparedList(){
        this(null);
    }

    public ComparedList(Comparator<T> comparator){
        this(comparator,null);
    }

    public ComparedList(Comparator<T> comparator,List<T> list){
        mList=list;
        mComparator=comparator;
    }

    public final ComparedList<T> add(T child){
        if (null!=child){
            List<T> list=mList;
            list=null!=list?list:(mList=new ArrayList<>());
            Comparator<T> comparator=mComparator;
            if (null!=comparator){
                int size=list.size();T childData=null;
                for (int i = 0; i < size; i++) {
                    if (null==(childData=list.get(i))){
                        continue;
                    }else if (comparator.compare(child,childData)<=0){
                        list.add(i,child);
                        return this;
                    }
                }
            }
            list.add(child);
        }
        return this;
    }

    public List<T> getList() {
        return mList;
    }
}
