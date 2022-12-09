package com.luckmerlin.browser.file;

import android.os.Parcel;
import com.luckmerlin.adapter.PageListAdapter;
import com.luckmerlin.browser.Label;
import com.luckmerlin.data.Parcelable;
import com.luckmerlin.data.Parceler;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Folder implements PageListAdapter.Page<File>, Parcelable {
    private long mFrom;
    private File mFile;
    private ArrayList<File> mFiles;

    public Folder(){
        this(null);
    }

    public Folder(Object obj){
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

    public final Folder setFile(File file) {
        this.mFile = file;
        return this;
    }

    public File getFile(){
        return mFile;
    }

    public Folder setTotal(long total) {
        File file=mFile;
        if (null!=file){
            file.setTotal(total);
        }
        return this;
    }

    public Folder setUsedVolume(long total) {
        File file=mFile;
        if (null!=file){
            file.setUsedVolume(total);
        }
        return this;
    }

    public Folder setTotalVolume(long total) {
        File file=mFile;
        if (null!=file){
            file.setTotalVolume(total);
        }
        return this;
    }

    public final boolean isChild(Object pathObj, boolean parent){
        return isChild(pathObj,parent,false);
    }

    public final boolean isChild(Object pathObj,boolean parent,boolean recursion){
        File file=mFile;
        return null!=file&&file.isChild(pathObj,parent,recursion);
    }

    public boolean isLocalFile(){
        File file=mFile;
        return null!=file&&file.isLocalFile();
    }

    public long getTotalVolume(){
        File file=mFile;
        return null!=file?file.getTotalVolume():0;
    }

    public long getUsedVolume(){
        File file=mFile;
        return null!=file?file.getUsedVolume():0;
    }

    public long getLength(){
        File file=mFile;
        return null!=file?file.getLength():0;
    }

    public long getTotal(){
        File file=mFile;
        return null!=file?file.getTotal():0;
    }

    public int getPermission(){
        File file=mFile;
        return null!=file?file.getPermission():0;
    }

    public File generateFile(String path){
        File file=mFile;
        return null!=file?file.generateFile(path):null;
    }

    public String getPath(){
        File file=mFile;
        return null!=file?file.getPath():null;
    }

    public String getExtension(boolean include){
        File file=mFile;
        return null!=file?file.getExtension(include):null;
    }

    public String getHost(){
        File file=mFile;
        return null!=file?file.getHost():null;
    }

    public String getMime(){
        File file=mFile;
        return null!=file?file.getMime():null;
    }

    public String getName(){
        File file=mFile;
        return null!=file?file.getName():null;
    }

    public String getLogoUrl(){
        File file=mFile;
        return null!=file?file.getLogoUrl():null;
    }

    public String getParent(){
        File file=mFile;
        return null!=file?file.getParent():null;
    }

    public String getSep(){
        File file=mFile;
        return null!=file?file.getSep():null;
    }

    public String getThumb(){
        File file=mFile;
        return null!=file?file.getThumb():null;
    }

    public String getTitle(){
        File file=mFile;
        return null!=file?file.getTitle():null;
    }

    public long getModifyTime(){
        File file=mFile;
        return null!=file?file.getModifyTime():0;
    }

    public File getParentFile(){
        File file=mFile;
        return null!=file?file.getParentFile():null;
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

    ///////
    private Folder(Parceler parceler, Parcel parcel){
        mFrom=parceler.readLong(parcel,mFrom);
        mFrom=parceler.readParcelable(parcel);
        mFiles=parceler.readParcelable(parcel);
    }

    @Override
    public void writeToParcel(Parceler parceler, Parcel parcel, int flags) {
        parceler.writeLong(parcel,mFrom);
        parceler.writeParcelable(parcel,mFrom,flags);
        parceler.writeParcelable(parcel,mFiles,flags);
    }
}
