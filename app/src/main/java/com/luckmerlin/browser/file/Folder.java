package com.luckmerlin.browser.file;

import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonArray;
import com.merlin.adapter.PageListAdapter;
import org.json.JSONException;

import java.util.List;

public class Folder extends File implements PageListAdapter.Page<File> {

    public Folder(){
        super();
    }

    public Folder(Object json)   {
        super(json);
    }

    public JsonArray getChildrenArray(){
        return optJsonArray(Label.LABEL_CHILDREN);
    }

    public Folder setChildren(Object children){
        return setArraySafe(this,Label.LABEL_CHILDREN,children);
    }

    public final boolean isEmpty(){
        JsonArray array=getChildrenArray();
        return null==array||array.length()<=0;
    }

    public List<File> getChildren(){
        JsonArray array=getChildrenArray();
        try {
            return null!=array?array.getList((Object from)-> null!=from?new File(from):null):null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<File> getPageData() {
        return getChildren();
    }
}
