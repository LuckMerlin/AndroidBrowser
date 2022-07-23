package com.luckmerlin.browser.file;

import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonArray;
import java.util.List;

public class Folder extends File{

    public Folder(){
        this(null);
    }

    public Folder(Object json){
        super(json);
    }

    public JsonArray getChildrenArray(){
        return optJsonArray(Label.LABEL_CHILDREN);
    }

    public List<File> getChildren(){
        JsonArray array=getChildrenArray();
        return null!=array?array.getList((Object from)-> null!=from?new File(from):null):null;
    }


}
