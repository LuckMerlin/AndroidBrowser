package com.luckmerlin.browser.file;

import android.os.Parcel;
import android.os.Parcelable;

import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonArray;
import com.merlin.adapter.PageListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Folder extends File implements PageListAdapter.Page<File>, Parcelable {
    private long mFrom;
    private ArrayList<File> mFiles;

    public Folder(JSONObject jsonObject){
        super(jsonObject);
        JSONArray jsonArray=null!=jsonObject?jsonObject.optJSONArray(Label.LABEL_CHILDREN):null;
        setChildren(null!=jsonArray?new JsonArray(jsonArray).getList((Object from)->
                null!=from&&from instanceof JSONObject?new File((JSONObject)from ):null):null);
    }

    public Folder(File file){
        super(file);
    }

    public Folder setChildren(ArrayList<File> files){
        mFiles=files;
        return this;
    }

    public Folder setFrom(long from) {
        this.mFrom = from;
        return this;
    }

    public int getVolumePercent(){
        long total=getTotalVolume();
        long used=getUsedVolume();
        used=used>=0?used:0;
        total=total>=0?total:0;
        return total>0?(int)(used*100.0d/total):0;
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

    public boolean isQueryFinish(){
        return getEnd()>=getTotal();
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

    /////////////////////////////
    private Folder(Parcel in) {
        mFrom = in.readLong();
        mFiles=in.readArrayList(in.getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mFrom);
        ArrayList<File> files=mFiles;
        dest.writeParcelableArray(null!=files?files.toArray(new File[files.size()]):null,0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };
}
