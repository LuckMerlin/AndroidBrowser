package com.luckmerlin.task;

import com.luckmerlin.browser.Code;
import com.luckmerlin.core.CodeResult;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.MatcherInvoker;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Result;
import com.luckmerlin.core.Section;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
//
public class TaskGroup extends AbstractTask implements Executor{
    private final MatcherInvoker mMatchInvoker=new MatcherInvoker();

    public TaskGroup(Progress progress) {
        super(progress);
    }

    @Override
    protected Result onExecute() {
        return null;
    }

    @Override
    public boolean execute(Task task, OnProgressChange callback) {
        return false;
    }

    @Override
    public void match(Matcher<TaskExecutor.ExecuteTask> matcher) {
//        mMatchInvoker.match()
    }

    //    private final Map<A,Object> mExecutedMap=new HashMap<>();
//    private A mExecuting=null;
//
//    public TaskGroup(Progress progress) {
//        super(progress);
//    }
//
//    @Override
//    protected Result onExecute(Object arg) {
//        while (true){
//            A next= next();
//            if (null==next){
//                return  new Reply().set(Code.CODE_SUCCEED,"None next.",null);
//            }
//            final OnProgressChange innerProgress=(Task task, Progress progress)-> {
//                notifyProgress(task,progress);
//            };
//            setTaskValue(next,innerProgress);
//            mExecuting=next;
//            CodeResult result=next.execute(arg,innerProgress);
//            mExecuting=null;
//            result=null!=result?result:new Reply().set(Code.CODE_FAIL,"Task none result.",null);
//            setTaskValue(next,result);
//        }
//    }
//
//    @Override
//    public final A getExecuting() {
//        return mExecuting;
//    }
//
//    private final boolean setTaskValue(A task, Object result){
//        Map<A,Object> map=null!=task?mExecutedMap:null;
//        if (null!=map){
//            synchronized (map){
//                map.put(task,result);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public final A next(){
//        Map<A,Object> executedMap=mExecutedMap;
//        if (null!=executedMap) {
//            synchronized (executedMap) {
//                Set<A> set = executedMap.keySet();
//                for (A child : set) {
//                    if (null != child && null == executedMap.get(child)) {
//                        return child;
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public final A find(Object task){
//        Map<A,Object> map=null!=task?mExecutedMap:null;
//        if (null!=map){
//            synchronized (map){
//                Set<A> set=map.keySet();
//                if (null!=set){
//                    for (A child:set) {
//                        if (null!=child&&child.equals(task)){
//                            return child;
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public final boolean add(A task){
//        if (null!=task){
//            Map<A,Object> executedMap=mExecutedMap;
//            synchronized (executedMap){
//                if (!executedMap.containsKey(task)){
//                    executedMap.put(task,null);
//                    return true;
//                }
//            }
//        }
//       return false;
//    }
//
//    @Override
//    public final boolean remove(Object task){
//        if (null!=task){
//            Map<A,Object> executedMap=mExecutedMap;
//            synchronized (executedMap){
//                executedMap.remove(task);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public CodeResult<Section<A>> load(A from, Matcher<A> matcher) {
//        return null;
//    }
}
