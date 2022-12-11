package com.luckmerlin.browser;

import com.luckmerlin.browser.client.Client;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.task.Executor;

public interface BrowserExecutor extends Executor {
    boolean client(Matcher<Client> matcher);
    boolean saveClient(Client client,boolean delete);
}
