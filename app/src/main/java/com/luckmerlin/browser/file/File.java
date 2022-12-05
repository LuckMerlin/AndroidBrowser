package com.luckmerlin.browser.file;

import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.MimeTypeMap;
import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.task.Brief;
import org.json.JSONObject;

public class File implements Brief,Permission, Parcelable {
    private String mHost;
    private long mUsedVolume;
    private long mTotalVolume;
    private long mModifyTime;
    private long mLength;
    private String mThumb;
    private String mName;
    private String mMime;
    private int mPermission=PERMISSION_NONE;
    private long mTotal;
    private String mParent;
    private String mSep;

    public File(){
        this(null);
    }

    public File(Object obj){
        apply(obj);
    }

    private void apply(Object obj){
        if (null==obj){

        }else if(obj instanceof File){
            File file=(File)obj;
            mHost=file.mHost;
            mUsedVolume=file.mUsedVolume;
            mTotalVolume=file.mTotalVolume;
            mModifyTime=file.mModifyTime;
            mLength=file.mLength;
            mThumb=file.mThumb;
            mName=file.mName;
            mMime=file.mMime;
            mPermission=file.mPermission;
            mTotal=file.mTotal;
            mParent=file.mParent;
            mSep=file.mSep;
        }else if(obj instanceof JSONObject){
            JSONObject json=(JSONObject)obj;
            mHost=json.optString(Label.LABEL_HOST,mHost);
            mUsedVolume=json.optLong(Label.LABEL_USED_VOLUME,mUsedVolume);
            mTotalVolume=json.optLong(Label.LABEL_TOTAL_VOLUME,mTotalVolume);
            mThumb=json.optString(Label.LABEL_THUMB,mThumb);
            mPermission=json.optInt(Label.LABEL_PERMISSION,mPermission);
            mName=json.optString(Label.LABEL_NAME,mName);
            mMime=json.optString(Label.LABEL_MIME,mMime);
            mModifyTime=json.optLong(Label.LABEL_MODIFY_TIME,mModifyTime);
            mLength=json.optLong(Label.LABEL_LENGTH,mLength);
            mTotal=json.optLong(Label.LABEL_SIZE,mTotal);
            mParent=json.optString(Label.LABEL_PARENT,mParent);
            mSep=json.optString(Label.LABEL_SEP,mSep);
        }
    }

    public static File fromJson(Object json){
        return fromJson(json,null);
    }

    public static File fromJson(Object json,String name){
        if (null==json){
            return null;
        }else if(json instanceof JSONObject){
            json=null!=name&&name.length()>0?((JSONObject)json).optJSONObject(name):json;
            return new File((JSONObject)json);
        }
        return fromJson(JsonObject.makeJson(json),name);
    }

    public String getHost() {
        String host= mHost;
        host=null!=host?host.trim():null;
        return null!=host&&host.length()>0&&Character.isDigit(host.charAt(0))?"http://"+host:host;
    }

    public File setHost(String host){
        mHost=host;
        return this;
    }

    public final boolean isLocalFile(){
        return getHost()==null;
    }

    public final long getUsedVolume(){
        return mUsedVolume;
    }

    public final long getTotalVolume(){
        return mTotalVolume;
    }

    public File getParentFile(){
        return generateFile(getParent());
    }

    public String getThumb(){
        return mThumb;
    }

    public File setThumb(String thumb){
        mThumb=thumb;
        return this;
    }

    public String getExtension(boolean include){
        if (isDirectory()){
            return null;
        }
        String name=getName();
        int index=null!=name&&name.length()>0?name.lastIndexOf("."):-1;
        return index>0&&(include?index:++index)<name.length()?name.substring(index):null;
    }

    public File generateFile(String path){
        String sep=getSep();
        if (null==path||path.length()<=0||null==sep||sep.length()<=0){
            return null;
        }
        int index=path.lastIndexOf(sep);
        if (index<0||index+1>=path.length()){
            return new File(this).setParent(path).setName("");
        }
        String parent=index<=0?sep:path.substring(0,index);
        String name=path.substring(index+1);
        return new File(this).setParent(parent).setName(name);
    }

    public File setUsedVolume(long usedVolume){
        mUsedVolume=usedVolume;
        return this;
    }

    public File setReadable(boolean readable){
        return setPermission(readable?getPermission()|PERMISSION_READ:getPermission()&~PERMISSION_READ);
    }

    public File setWriteable(boolean writeable){
        return setPermission(writeable?getPermission()|PERMISSION_WRITE:getPermission()&~PERMISSION_WRITE);
    }

    public File setExecutable(boolean executable){
        return setPermission(executable?getPermission()|PERMISSION_EXECUTE:getPermission()&~PERMISSION_EXECUTE);
    }

    public boolean isReadable(){
        return (getPermission()&PERMISSION_READ)>0;
    }

    public boolean isExecutable(){
        return (getPermission()&PERMISSION_EXECUTE)>0;
    }

    public boolean isWriteable(){
        return (getPermission()&PERMISSION_WRITE)>0;
    }

    public File setPermission(int permission){
        mPermission=permission;
        return this;
    }

    public int getPermission(){
        return mPermission;
    }

    public File setTotalVolume(long totalVolume){
        mTotalVolume=totalVolume;
        return this;
    }

    public String getName() {
        return mName;
    }

    public File setName(String name){
        mName=name;
        return this;
    }

    public boolean isHostEquals(String host){
        String current=getHost();
        current=null!=current?current.trim():null;
        host=null!=host?host.trim():null;
        return (null==host&&null==current)||(null!=host&&null!=current&&current.equals(host));
    }

    public String getMime(){
        String mime=mMime;
        if (!isDirectory()&&(null==mime||mime.length()<=0)){
            mime=getExtension(false);
            mime=null!=mime&&mime.length()>0?MimeTypeMap.getSingleton().getMimeTypeFromExtension(mime):null;
        }
        return mime;
    }

    public File childFile(String childName){
        return null!=childName&&childName.length()>0?
                new File(this).setParent(getPath()).setName(childName):null;
    }

    public File setMime(String mime){
        mMime=mime;
        return this;
    }

    public long getModifyTime() {
        return mModifyTime;
    }

    public File setModifyTime(long modifyTime){
        mModifyTime=modifyTime;
        return this;
    }

    public long getLength() {
        return mLength;
    }

    public File setLength(long length){
        mLength=length;
        return this;
    }

    public long getTotal() {
        return mTotal;
    }

    public File setTotal(long total){
        mTotal=total;
        return this;
    }

    public boolean isDirectory(){
        return getTotal()>=0;
    }

    public String getParent(){
        return mParent;
    }

    public File setParent(String parent){
        mParent=parent;
        return this;
    }

    public String getSep(){
        return mSep;
    }

    public File setSep(String sep){
        mSep=sep;
        return this;
    }

    public final boolean isChild(Object pathObj,boolean parent){
        return isChild(pathObj,parent,false);
    }

    public final boolean isChild(Object pathObj,boolean parent,boolean recursion){
        if (null==pathObj){
            return false;
        }else if (pathObj instanceof File){
            return isChild(((File)pathObj).getPath(),parent,recursion);
        }else if (!(pathObj instanceof String)){
            return false;
        }
        String path=(String)pathObj;
        if (path.length()<=0){
            return false;
        }
        String current=parent?getParent():getPath();
        return null!=current&&(recursion?path.startsWith(current):path.equals(current));
    }

    public String getPath(){
        String parent=getParent();
        String sep=getSep();
        String name=getName();
        return null!=parent&&null!=sep&&null!=name?(parent.equals(sep)?"":parent)+sep+name:null;
    }

    public boolean isPatchEquals(String path){
        String currentPath=getPath();
        return (null==path&&null==currentPath)||(null!=currentPath&&null!=path&&currentPath.equals(path));
    }

    public interface Type{
        public final static String VIDEO="video/";
        public final static String IMAGE="image/";
        public final static String AUDIO="audio/";
        public final static String APK="application/vnd.android";
    }

    public boolean isType(String type){
        return isType(getMime(),type);
    }

    public static boolean isType(String mime,String type){
        return null!=mime&&null!=type&&mime.startsWith(type);
    }

    @Override
    public String getLogoUrl() {
        return null;
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public boolean equals( Object obj) {
        if (null==obj){
            return false;
        }else if (obj instanceof File){
            return isPatchEquals(((File)obj).getPath());
        }else if (obj instanceof String){
            return isPatchEquals((String)obj);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "File{"+getPath()+"}";
    }
    ///////////////
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mHost);
        dest.writeString(mThumb);
        dest.writeString(mParent);
        dest.writeString(mSep);
        dest.writeString(mName);
        dest.writeString(mMime);
        dest.writeLong(mPermission);
        dest.writeLong(mUsedVolume);
        dest.writeLong(mTotalVolume);
        dest.writeLong(mModifyTime);
        dest.writeLong(mLength);
        dest.writeLong(mTotal);
    }

    public static final Creator<File> CREATOR = new Creator<File>() {
        @Override
        public File createFromParcel(Parcel parcel) {
            File file=new File();
            file.mHost=parcel.readString();
            file.mThumb=parcel.readString();
            file.mParent=parcel.readString();
            file.mSep=parcel.readString();
            file.mName=parcel.readString();
            file.mMime=parcel.readString();
            file.mPermission=parcel.readInt();
            file.mUsedVolume=parcel.readLong();
            file.mTotalVolume=parcel.readLong();
            file.mModifyTime=parcel.readLong();
            file.mLength=parcel.readLong();
            file.mTotal=parcel.readLong();
            return file;
        }

        @Override
        public File[] newArray(int size) {
            return new File[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
