package com.luckmerlin.browser.file;

import android.os.Parcel;

import com.luckmerlin.adapter.PageListAdapter;
import com.luckmerlin.browser.Label;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Folder extends File implements PageListAdapter.Page<File> {
    private long mFrom;
    private ArrayList<File> mFiles;

    public Folder(){
        this(null);
    }

    public Folder(Object obj){
        super(obj);
        apply(obj);
    }

    private void apply(Object obj){
        if (null==obj){

        }else if (obj instanceof Folder){
            mFrom=((Folder)obj).mFrom;
            mFiles=((Folder)obj).mFiles;
        }else if (obj instanceof JSONObject){
            JSONObject json=(JSONObject)obj;
            mFrom=json.optLong(Label.LABEL_FROM,-1);
            JSONArray array=json.optJSONArray(Label.LABEL_CHILDREN);
            ArrayList files=null;
            if (null!=array){
                int length=array.length();
                files=(length<=0?null:new ArrayList<>(length));
                JSONObject jsonObject=null;
                for (int i = 0; i < length; i++) {
                    if (null!=(jsonObject=array.optJSONObject(i))){
                        files.add(new File(jsonObject));
                    }
                }
            }
            mFiles=files;
        }
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

    @Override
    public void onParcelRead(Parcel parcel) {
        super.onParcelRead(parcel);
        mFrom=parcel.readLong();
        ArrayList<File> files=new ArrayList<>();
        Parceler.readList(parcel,files,null);
        mFiles=files;
    }

    @Override
    public void onParcelWrite(Parcel parcel) {
        super.onParcelWrite(parcel);
        parcel.writeLong(mFrom);
        Parceler.writeList(parcel,mFiles);
    }
}
