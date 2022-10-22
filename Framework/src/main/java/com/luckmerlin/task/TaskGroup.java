package com.luckmerlin.task;

import com.luckmerlin.core.Code;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskGroup extends AbstractTask{
    private final Map<Task,Boolean> mQueue=new HashMap<>();
    private Task mExecuting;

    public TaskGroup() {
        this(null);
    }

    public TaskGroup(Progress progress) {
        super(progress);
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        while (true){
            Task next= next();
            if (null==next){
                Task task=findFirstFail();
                Result result=null!=task?task.getResult():null;
                return null!=result?result:new Response().set(Code.CODE_SUCCEED,"None next.",null);
            }
            final OnProgressChange innerProgress=(Task task, Progress progress)-> {
                notifyProgress(task,progress);
            };
            mQueue.put(next,true);
            mExecuting=next;
            next.execute(runtime,innerProgress);
            mExecuting=null;
            mQueue.put(next,false);
        }
    }

    public final TaskGroup add(Task... tasks){
        Map<Task,Boolean> queue=mQueue;
        return null!=queue?match(tasks, (Task data)->null!=data&&null!=queue.put(data,queue.get(data)))?this:this:this;
    }

    public final TaskGroup add(Collection<Task> task){
        Map<Task,Boolean> queue=mQueue;
        return null!=queue?match(task, (Task data)->null!=data&&null!=queue.put(data,queue.get(data)))?this:this:this;
    }

    public final int getSize(){
        Map<Task,Boolean> queue=mQueue;
        return null!=queue?queue.size():-1;
    }

    public final Task findFirstFail(){
        Map<Task,Boolean> queue=mQueue;
        return null!=queue?findFirst((Task data)-> {
            Boolean isExecute=null!=data?queue.get(data):null;
            Progress progress=null!=isExecute&&!isExecute?data.getProgress():null;
            return null==progress||!progress.isSucceed();
        }):null;
    }

    public final Task getExecuting() {
        return mExecuting;
    }

    public final Task next(){
        Map<Task,Boolean> queue=mQueue;
        if (null!=queue) {
            synchronized (queue) {
                Set<Task> set = queue.keySet();
                for (Task child : set) {
                    if (null != child && null == queue.get(child)) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    public final Task find(Object obj){
        return findFirst((Task data)->null!=data&&(null==obj||data.equals(obj)));
    }

    public final Task findFirst(Matcher<Task> matcher){
        List<Task> matched= find(matcher,1);
        return null!=matched&&matched.size()>0?matched.get(0):null;
    }

    public final List<Task> find(Matcher<Task> matcher,int limit){
        MatchedCollector<Task> collector=new MatchedCollector<Task>(limit).setMatcher(matcher);
        return match(collector)?collector.getMatched():null;
    }

    public final boolean match(Matcher<Task> matcher){
        Map<Task,Boolean> queue=mQueue;
        return null!=queue&&match(queue.keySet(),matcher);
    }

    public final List<Task> remove(Object taskObj,int limit){
        Map<Task,Boolean> queue=null!=taskObj?mQueue:null;
        if (null==queue){
            return null;
        }
        MatchedCollector<Task> collector= new MatchedCollector<Task>(limit).setMatcher((data)->null!=data&&data.equals(taskObj));
        match(queue.keySet(),collector);
        List<Task> tasks=collector.getMatched();
        match(tasks,(Task data)-> null!=data&&queue.remove(data));
        return tasks;
    }

}
