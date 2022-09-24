package com.luckmerlin.browser.file;

import android.webkit.MimeTypeMap;
import androidx.annotation.Nullable;
import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.utils.FileSize;
import com.luckmerlin.core.Brief;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.json.Json;
import com.luckmerlin.json.JsonObject;

import org.json.JSONObject;

import java.nio.file.Files;
import java.util.logging.Handler;

public class File extends JsonObject implements Brief {

    public File(){
        super();
    }

    public File(JSONObject json){
        super(json);
    }

    public static File fromJson(Object json){
        if (null==json){
            return null;
        }else if(json instanceof JSONObject){
            return new File((JSONObject)json);
        }
        return fromJson(JsonObject.makeJson(json));
    }

    public String getHost() {
        String host= optString(Label.LABEL_HOST,null);
        host=null!=host?host.trim():null;
        return null!=host&&host.length()>0&&Character.isDigit(host.charAt(0))?"http://"+host:host;
    }

    public File setHost(String host){
        return putSafe(this,Label.LABEL_HOST,host);
    }

    public final boolean isLocalFile(){
        return getHost()==null;
    }

    public final long getAvailableVolume(){
        return optLong(Label.LABEL_AVAILABLE);
    }

    public final long getTotalVolume(){
        return optLong(Label.LABEL_TOTAL);
    }

    public File getParentFile(){
        return generateFile(getParent());
    }

    public String getThumb(){
        return optString("thumb",null);
    }

    public File setThumb(String thumb){
        return putSafe(this,"thumb",thumb);
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

    public File setAvailableVolume(long children){
        return putSafe(this,Label.LABEL_AVAILABLE,children);
    }

    public File setTotalVolume(long children){
        return putSafe(this,Label.LABEL_TOTAL,children);
    }

    public String getName() {
        return optString(Label.LABEL_NAME,null);
    }

    public File setName(String name){
        return putSafe(this,Label.LABEL_NAME,name);
    }

    public boolean isHostEquals(String host){
        String current=getHost();
        current=null!=current?current.trim():null;
        host=null!=host?host.trim():null;
        return (null==host&&null==current)||(null!=host&&null!=current&&current.equals(host));
    }

    public String getMime(){
        String mime=optString(Label.LABEL_MIME,null);
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
    public CharSequence getNote() {
        return FileSize.formatSizeText(getLength());
    }

    @Override
    public final Object getIcon() {
        return null;
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
