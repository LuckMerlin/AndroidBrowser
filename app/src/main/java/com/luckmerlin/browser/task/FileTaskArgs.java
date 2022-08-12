package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonObject;

public class FileTaskArgs extends JsonObject {

    public final boolean isDirectExecute(boolean direct){
        return optBoolean(Label.LABEL_ACCESS,direct);
    }

    public final FileTaskArgs setDirectExecute(boolean direct){
        return putSafe(this,Label.LABEL_ACCESS,direct);
    }
}
