package com.luckmerlin.browser.file;

import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonArray;
import com.luckmerlin.object.Parser;
import com.merlin.adapter.PageListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Folder extends File implements PageListAdapter.Page<File> {
    private long mFrom;
    private long mTotal;
    private List<File> mFiles;

    public Folder(JSONObject jsonObject){
        super(jsonObject);
        JSONArray jsonArray=null!=jsonObject?jsonObject.optJSONArray(Label.LABEL_CHILDREN):null;
        setChildren(null!=jsonArray?new JsonArray(jsonArray).getList((Object from)->
                null!=from&&from instanceof JSONObject?new File((JSONObject)from ):null):null);
    }

    public Folder(File file){
        super(file);
    }

    public Folder setChildren(List<File> files){
        mFiles=files;
        return this;
    }

    public Folder setFrom(long from) {
        this.mFrom = from;
        return this;
    }

    public Folder setTotal(long total) {
        this.mTotal = total;
        return this;
    }

    public long getFrom() {
        return mFrom;
    }

    public long getEnd(){
        long from=mFrom;
        List<File> files=mFiles;
        return from>=0?from+(null!=files?files.size():0):-1;
    }

    public int getSize(){
        List<File> files=mFiles;
        return null!=files?files.size():0;
    }

    public long getTotal() {
        return mTotal;
    }

    public boolean isQueryFinish(){
        return getEnd()>=mTotal;
    }

    public final boolean isEmpty(){
        List<File> files=mFiles;
        return null==files||files.size()<=0;
    }

    public List<File> getChildren(){
        return mFiles;
    }

    @Override
    public List<File> getPageData() {
        return getChildren();
    }
}
