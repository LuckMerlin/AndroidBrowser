package com.luckmerlin.browser.task;

import com.luckmerlin.browser.BrowserExecutor;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnInitialOption;
import com.luckmerlin.task.Progress;

@Deprecated
public abstract class FileTask extends AbstractTask {
    private boolean mDeleteSucceedEnable;

    public FileTask(Progress progress) {
        super(progress);
    }

    public final FileTask enableDeleteSucceed(boolean enable){
        mDeleteSucceedEnable=enable;
        return this;
    }

    public final Client getFileClient(File file){
        if (null==file){
            return null;
        }
        MatchedCollector<Client> collector=new MatchedCollector<Client>(1).
                setMatcher((obj)->null!=obj&&file.isHostEquals(obj.getHost())?true:false);
        return client(collector)?collector.getFirstMatched():null;
    }

    public final boolean client(Matcher<Client> matcher){
        Executor executor=getExecutor();
        return null!=executor&&executor instanceof BrowserExecutor &&((BrowserExecutor)executor).client(matcher);
    }

}
