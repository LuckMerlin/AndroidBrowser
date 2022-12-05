package com.luckmerlin.browser.task;

import com.luckmerlin.browser.BrowserExecutor;
import com.luckmerlin.browser.client.Client;
import com.luckmerlin.browser.ClientMeta;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Executor;

public abstract class AbstractFileTask extends AbstractTask {

    public final Client getFileClient(File file){
        if (null==file){
            return null;
        }
        String host=file.getHost();
        MatchedCollector<Client> collector=new MatchedCollector<Client>(1).setMatcher((obj)->{
            ClientMeta meta=null!=obj?obj.getMeta():null;
            String childHost=null!=meta?meta.getHost():null;
            return (null==host&&null==childHost)||(null!=host&&null!=childHost&&host.equals(childHost));
        });
        return client(collector)?collector.getFirstMatched():null;
    }

    public final boolean client(Matcher<Client> matcher){
        Executor executor=getExecutor();
        return null!=executor&&executor instanceof BrowserExecutor &&((BrowserExecutor)executor).client(matcher);
    }

}
