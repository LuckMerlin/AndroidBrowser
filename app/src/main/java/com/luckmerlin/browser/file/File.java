package com.luckmerlin.browser.file;

import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonObject;

public class File extends JsonObject{

    public File(){
        this(null);
    }

    public File(Object json){
        super(json);
    }

    public String getName() {
        return optString(Label.LABEL_NAME,null);
    }

    public String getMime(){
        return optString(Label.LABEL_MIME,null);
    }

    public long getModifyTime() {
        return optLoong(Label.LABEL_MODIFY_TIME,0);
    }

    public long getLength() {
        return optLoong(Label.LABEL_LENGTH,0);
    }

    public long getTotal() {
        return optLoong(Label.LABEL_SIZE,-1);
    }

    public boolean isDirectory(){
        return getTotal()>=0;
    }

    public String getParent(){
        return optString(Label.LABEL_PARENT,null);
    }

    public String getSep(){
        return optString(Label.LABEL_SEP,null);
    }

    public String getPath(){
        String parent=getParent();
        String sep=getSep();
        String name=getName();
        return null!=parent&&null!=sep&&null!=name?parent+sep+name:null;
    }

}
