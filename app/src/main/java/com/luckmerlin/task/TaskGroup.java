package com.luckmerlin.task;

import com.luckmerlin.browser.Code;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Result;
import com.luckmerlin.core.Section;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TaskGroup<A extends Task<Object,?extends Result>, R extends Result>
        extends AbstractTask<Object,R>
        implements TaskExecutor<A>,Task<Object,R>{
    private final Map<A,Object> mExecutedMap=new HashMap<>();
    private A mExecuting=null;

    public TaskGroup(Progress progress) {
        super(progress);
    }

    @Override
    protected R onExecute(Object arg) {
        while (true){
            A next= next();
            if (null==next){
                return (R) new Reply().set(Code.CODE_SUCCEED,"None next.",null);
            }
            final OnProgressChange innerProgress=(Task task, Progress progress)-> {
                notifyProgress(task,progress);
            };
            setTaskValue(next,innerProgress);
            mExecuting=next;
            Result result=next.execute(arg,innerProgress);
            mExecuting=null;
            result=null!=result?result:new Reply().set(Code.CODE_FAIL,"Task none result.",null);
            setTaskValue(next,result);
        }
    }

    @Override
    public final A getExecuting() {
        return mExecuting;
    }

    private final boolean setTaskValue(A task, Object result){
        Map<A,Object> map=null!=task?mExecutedMap:null;
        if (null!=map){
            synchronized (map){
                map.put(task,result);
                return true;
            }
        }
        return false;
    }

    public final A next(){
        Map<A,Object> executedMap=mExecutedMap;
        if (null!=executedMap) {
            synchronized (executedMap) {
                Set<A> set = executedMap.keySet();
                for (A child : set) {
                    if (null != child && null == executedMap.get(child)) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public final A find(Object task){
        Map<A,Object> map=null!=task?mExecutedMap:null;
        if (null!=map){
            synchronized (map){
                Set<A> set=map.keySet();
                if (null!=set){
                    for (A child:set) {
                        if (null!=child&&child.equals(task)){
                            return child;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public final boolean add(A task){
        if (null!=task){
            Map<A,Object> executedMap=mExecutedMap;
            synchronized (executedMap){
                if (!executedMap.containsKey(task)){
                    executedMap.put(task,null);
                    return true;
                }
            }
        }
       return false;
    }

    @Override
    public final boolean remove(Object task){
        if (null!=task){
            Map<A,Object> executedMap=mExecutedMap;
            synchronized (executedMap){
                executedMap.remove(task);
                return true;
            }
        }
        return false;
    }

    @Override
    public Result<Section<A>> load(A from, Matcher<A> matcher) {
        return null;
    }
}
