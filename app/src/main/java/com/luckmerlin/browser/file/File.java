package com.luckmerlin.browser.file;

import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonObject;

import org.json.JSONException;

public class File extends JsonObject {

    public File(){
        super();
    }

    public File(Object json){
        super(json);
    }

    public String getHost() {
        return optString(Label.LABEL_HOST,null);
    }

    public File setHost(String host){
        return putSafe(this,Label.LABEL_HOST,host);
    }

    public final boolean isLocalFile(){
        return getHost()==null;
    }

    public String getName() {
        return optString(Label.LABEL_NAME,null);
    }

    public File setName(String name){
        return putSafe(this,Label.LABEL_NAME,name);
    }

    public String getMime(){
        String mime=optString(Label.LABEL_MIME,null);
        if (!isDirectory()&&(null==mime||mime.length()<=0)){
            mime=getName();
            mime= null!=mime&&mime.length()>0?MimeTypeMap.getFileExtensionFromUrl(mime):null;
            mime=null!=mime&&mime.length()>0?MimeTypeMap.getSingleton().getMimeTypeFromExtension(mime):null;
        }
        return mime;
    }

    public File childFile(String childName){
        return null!=childName&&childName.length()>0?new File(this).setParent(getPath()).setName(childName):null;
    }

    public File setMime(String mime){
        return putSafe(this,Label.LABEL_MIME,mime);
    }

    public long getModifyTime() {
        return optLong(Label.LABEL_MODIFY_TIME,0);
    }

    public File setModifyTime(long modifyTime){
        return putSafe(this,Label.LABEL_MODIFY_TIME,modifyTime);
    }

    public long getLength() {
        return optLong(Label.LABEL_LENGTH,0);
    }

    public File setLength(long length){
        return putSafe(this,Label.LABEL_LENGTH,length);
    }

    public long getTotal() {
        return optLong(Label.LABEL_SIZE,-1);
    }

    public File setTotal(long total){
        return putSafe(this,Label.LABEL_SIZE,total);
    }

    public boolean isDirectory(){
        return getTotal()>=0;
    }

    public String getParent(){
        return optString(Label.LABEL_PARENT,null);
    }

    public File setParent(String parent){
        return putSafe(this,Label.LABEL_PARENT,parent);
    }

    public String getSep(){
        return optString(Label.LABEL_SEP,null);
    }

    public File setSep(String sep){
        return putSafe(this,Label.LABEL_SEP,sep);
    }

    public final boolean isChild(Object pathObj,boolean parent){
        if (null==pathObj){
            return false;
        }else if (pathObj instanceof File){
            return isChild(((File)pathObj).getPath(),parent);
        }else if (!(pathObj instanceof String)){
            return false;
        }
        String path=(String)pathObj;
        if (path.length()<=0){
            return false;
        }
        String current=parent?getParent():getPath();
        return null!=current&&path.startsWith(current);
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (null==obj){
            return false;
        }else if (obj instanceof File){
            return isPatchEquals(((File)obj).getPath());
        }else if (obj instanceof String){
            return isPatchEquals((String)obj);
        }
        return super.equals(obj);
    }
}
