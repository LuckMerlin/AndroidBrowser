package com.luckmerlin.browser.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Response;
import com.luckmerlin.data.ComparedList;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.utils.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class LocalClient extends AbstractClient {
    private String mRootPath="/sdcard";

    @Override
    public String getName() {
        return "Local";
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public Response<Folder> listFiles(File folder, long start, int size, Filter filter) {
        size=size<=0?10:size;
        start=start<=0?0:start;
        String pathValue=null!=folder?folder.getPath():null;
        String browserPath=null!=pathValue&&pathValue.length()>0?pathValue:mRootPath;
        final java.io.File browserFile=null!=browserPath&&browserPath.length()>0?new java.io.File(browserPath):null;
        if (null==browserFile){
            Debug.W("Can't load client while query file invalid."+browserPath);
            return new Response<Folder>().setCode(Code.CODE_ARGS_INVALID).setMsg("Query file invalid.");
        }else if (!browserFile.exists()){
            Debug.W("Can't load client while query file not exist."+browserPath);
            return new Response<Folder>().setCode(Code.CODE_NOT_EXIST).setMsg("Query file not exist.");
        }else if (!browserFile.isDirectory()){
            Debug.W("Can't load client while query file not directory.");
            return new Response<Folder>().setCode(Code.CODE_ARGS_INVALID).setMsg("Query file not directory.");
        }
        String filterName=null!=filter?filter.getName():null;
        Debug.D("Loading local client.name="+filterName+" from="+start+" path="+browserPath);
        final ComparedList<File> files=new ComparedList<>((File file1, File file2)-> {
            boolean directory1=file1.isDirectory();
            boolean directory2=file2.isDirectory();
            if (directory1&&directory2){
                String file1Name=null!=file1?file1.getName():null;
                String file2Name=null!=file2?file2.getName():null;
                file1Name=null!=file1Name?file1Name:"";
                file2Name=null!=file2Name?file2Name:"";
                return file1Name.compareTo(file2Name);
            }
            return directory1?-1:directory2?1:0;
        },null);
        browserFile.listFiles((java.io.File file)-> {
            if (null!=file){
                String fileName=file.getName();File child=null;
                if (null!=filterName&&filterName.length()>0&&(null==fileName||!fileName.contains(filterName))){
                    return false;
                }else if (null!=(child=createLocalFile(file))){
                    files.add(child);
                }
            }
            return false;
        });
        List<File> fileList=files.getList();
        long total=null!=fileList?fileList.size():0;
        Folder queryFiles=new Folder(createLocalFile(browserFile)).setFrom(start);
        queryFiles.setTotal(total);
        queryFiles.setAvailableVolume(browserFile.getFreeSpace()).setTotalVolume(browserFile.getTotalSpace());
        List<File> subFiles=total>0&&start<total?fileList.subList((int)start,Math.min((int)(start+size),(int)total)):null;
        return new Response<Folder>().set(Code.CODE_SUCCEED,"Succeed.", queryFiles.setChildren(subFiles));
    }

    @Override
    public Drawable loadThumb(View view, File file, Canceled canceled) {
        Context context=null!=view?view.getContext():null;
        String mime=null;String path=null;
        if (null==file||null==canceled||null==context||file.isDirectory()){
            return null;
        }else if (null==(mime=file.getMime())||null==(path=file.getPath())){
            return null;
        }else if (File.isType(mime, File.Type.IMAGE)){
            Bitmap bitmap=Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q?ThumbnailUtils.createImageThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND):null;
            bitmap=null!=bitmap?bitmap:loadFileBitmap(path,60,60);
            return null!=bitmap?new BitmapDrawable(bitmap):null;
        }else if (File.isType(mime,File.Type.AUDIO)){
            Bitmap bitmap=Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q?ThumbnailUtils.
                    createAudioThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND):null;
            return null!=bitmap?new BitmapDrawable(bitmap):null;
        }else if (File.isType(mime,File.Type.VIDEO)){
            Bitmap bitmap=Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q?ThumbnailUtils.
                    createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND):null;
            return null!=bitmap?new BitmapDrawable(bitmap):null;
        }else if (File.isType(mime,File.Type.APK)){

        }
        return null;
    }

    @Override
    public Response<File> createFile(File parent, String name, boolean isDir) {
        if (null==parent||!parent.isLocalFile()||null==name||name.length()<=0|| name.contains(java.io.File.separator)){
            Debug.W("Fail create file while parent or name invalid.parent="+parent+" name="+name);
            return new Response<File>().set(Code.CODE_ARGS_INVALID,"Parent or name invalid",null);
        }
        String path=parent.getPath();
        if (null==path||path.length()<=0){
            Debug.W("Fail create file while path invalid.path="+path);
            return new Response<File>().set(Code.CODE_ARGS_INVALID,"Path invalid",null);
        }
        java.io.File file=new java.io.File(path,name);
        if (file.exists()) {
            Debug.W("Fail create file while already exist.");
            boolean currentIsFile=file.isFile();
            boolean succeed=(isDir&&!currentIsFile)||(!isDir&&currentIsFile);
            return new Response<File>().set(succeed? Code.CODE_ALREADY:Code.CODE_EXIST, "Already exist",succeed?createLocalFile(file):null);
        }
        try {
            java.io.File parentFile=file.getParentFile();
            if (null!=parentFile&&!parentFile.exists()){
                parentFile.mkdirs();
            }
            boolean succeed=isDir?file.mkdir():file.createNewFile();
            Response<File> reply=file.exists()?new Response<File>().set(Code.CODE_SUCCEED,null,
                    createLocalFile(file)) :new Response<File>().set(Code.CODE_FAIL,"Fail",null);
            Debug.D("Finish create file."+succeed+" "+file);
            return reply;
        }catch (Exception e){
            Debug.W("Exception create file.e="+e);
            return new Response<File>().set(Code.CODE_ERROR,"Exception.e="+e,null);
        }
    }

    @Override
    public Response<File> deleteFile(File file, OnFileDoingUpdate update) {
        if (null==file||!file.isLocalFile()){
            Debug.D("Fail delete local client file while file arg invalid.");
            return new Response<File>().set(Code.CODE_ARGS_INVALID,"File invalid.",file);
        }
        String path=file.getPath();
        if (null==path||path.length()<=0){
            Debug.D("Fail delete local client file while file path invalid.");
            return new Response<File>().set(Code.CODE_ARGS_INVALID,"File path invalid.",file);
        }
        return deleteAndroidFile(new java.io.File(path),update);
    }

    @Override
    public Response<OutputStream> openOutputStream(File file) {
        String path=null;
        if (null==file||!file.isLocalFile()||null==(path=file.getPath())||path.length()<=0){
            Debug.W("Fail open file output stream while file invalid.file="+file);
            return new Response(Code.CODE_ARGS_INVALID, "File invalid");
        }
        java.io.File androidFile = new java.io.File(path);
        if (androidFile.isDirectory()) {
            Debug.W("Fail open file output stream while file is android directory.");
            return new Response(Code.CODE_ARGS_INVALID, "File is android directory");
        }
        OutputStream outputStream =null;
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(androidFile, true);
            outputStream = new OutputStream(androidFile.length()) {
                @Override
                public void close() throws IOException {
                    fileOutputStream.close();
                }

                @Override
                protected void onWrite(byte[] b, int off, int len) throws IOException {
                    fileOutputStream.write(b,off,len);
                }
            };
            outputStream.setTitle(androidFile.getName());
            return new Response<OutputStream>().set(Code.CODE_SUCCEED, "Succeed.", outputStream);
        }catch (Exception e){
            Utils.closeStream(outputStream);
            e.printStackTrace();
            return new Response<OutputStream>().set(Code.CODE_ERROR, "Exception open local file input stream.", null);
        }
    }

    @Override
    public Response<InputStream> openInputStream(long openLength, File file) {
        String path=null;
        if (null==file||!file.isLocalFile()||null==(path=file.getPath())||path.length()<=0){
            Debug.W("Fail open file input stream while file invalid.");
            return new Response(Code.CODE_ARGS_INVALID, "File invalid");
        }
        java.io.File androidFile = new java.io.File(path);
        if (androidFile.isDirectory()) {
            Debug.W("Fail open file input stream while file is android directory.");
            return new Response(Code.CODE_ARGS_INVALID, "File is android directory");
        }
        InputStream inputStream=null;
        try {
            final FileInputStream fileInputStream = new FileInputStream(androidFile);
            if (openLength<0){
                Debug.W("Fail open file input stream while file open length invalid.");
                return new Response(Code.CODE_ARGS_INVALID,"File open length invalid");
            }else if (openLength>0){
                Debug.D("Open file input stream with skip."+openLength);
                fileInputStream.skip(openLength);
            }
            inputStream=new InputStream(openLength){
                @Override
                public void close() throws IOException {
                    fileInputStream.close();
                }

                @Override
                public long length() {
                    return androidFile.length();
                }

                @Override
                public int onRead(byte[] b, int off, int len) throws IOException {
                    return fileInputStream.read(b,off,len);
                }
            };
            inputStream.setTitle(androidFile.getName());
            return new Response<InputStream>().set(Code.CODE_SUCCEED,"Succeed.",inputStream);
        } catch (Exception e) {
            Utils.closeStream(inputStream);
            e.printStackTrace();
            return new Response<InputStream>().set(Code.CODE_ERROR, "Exception open local file input stream.", null);
        }
    }

    @Override
    public boolean openFile(File openFile, Context context) {
        String filePath=null!=openFile?openFile.getPath():null;
        if (null==filePath||filePath.length()<=0){
            return false;
        }else if (!openFile.isLocalFile()){
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new java.io.File(filePath));
        intent.setDataAndType(uri, openFile.getMime());
        try {
            context.startActivity(intent);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Canceler setHome(File file, OnFinish<Reply<File>> onFinish) {
        return null;
    }

    public static File createLocalFile(java.io.File file){
        if (null==file){
            return null;
        }
        String parent=file.getParent();
        parent=null!=parent?parent: java.io.File.separator;
        File localFile=new File().setLength(file.length()).setSep(java.io.File.separator).
                setModifyTime(file.lastModified()).setParent(parent).setName(file.getName());
        long total=-1;
        if (file.isDirectory()){
            java.io.File[] files=file.listFiles();
            total=null!=files?files.length:0;
        }
        return localFile.setTotal(total);
    }

    private Response<File> deleteAndroidFile(java.io.File file,OnFileDoingUpdate update){
        if (null==file){
            Debug.W("Fail delete android file while path invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"Path invalid.");
        }else if (!file.exists()){
            Debug.W("Fail delete android file while path not exist.");
            return new Response(Code.CODE_NOT_EXIST,"Path not exist.");
        }
        Debug.D("Deleting android file."+file);
        Response response=doDeleteAndroidFile(file,update);
        if (null!=response&&!response.isSucceed()){
            Debug.D("Fail delete android file."+file);
            return response;
        }else if (file.exists()){
            Debug.D("Fail delete android file."+file);
            return new Response(Code.CODE_FAIL,"Fail delete.");
        }
        Debug.D("Succeed delete android file."+file);
        return new Response(Code.CODE_SUCCEED,null);
    }

    private Response doDeleteAndroidFile(java.io.File file, OnFileDoingUpdate update){
        if (null==file||!file.exists()){
            Debug.D("Fail delete android file."+file);
            return new Response(Code.CODE_ARGS_INVALID,"File not exist or invalid.");
        }
        File fileObj=LocalClient.createLocalFile(file);
        if (notifyDoingFile(Mode.MODE_DELETE,0,"Start delete.", fileObj,fileObj,update)){
            Debug.D("Fail delete android file while canceled.");
            return new Response(Code.CODE_CANCEL,"Canceled");
        }
        if (file.isDirectory()){
            java.io.File[] files=file.listFiles();
            int length=null!=files?files.length:-1;
            Response response=null;
            for (int i = 0; i < length; i++) {
                response=null==(response=doDeleteAndroidFile(files[i],update))?
                        new Response(Code.CODE_UNKNOWN,"Unknown error."):response;
                if (!response.isSucceed()){
                    return response;
                }
            }
        }
        file.delete();
        boolean notExist=!file.exists();
        notifyDoingFile(Mode.MODE_DELETE,notExist?100:0,"Finish delete.", fileObj,fileObj,update);
        return new Response(notExist?Code.CODE_SUCCEED:Code.CODE_FAIL,"Finish");
    }

    private Bitmap loadFileBitmap(String filePath,int with,int height){
        if (null==filePath||filePath.length()<=0){
            return null;
        }else if (with<=0||height<=0){
            return null;
        }
        try {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(filePath,options);
            int minSideLength=Math.min(with,height);
            //
            int maxNumOfPixels=with*height;
            double w=options.outWidth;
            double h=options.outHeight;
            int lowerBound=maxNumOfPixels<=0?1:(int)Math.ceil(Math.sqrt(w*h/maxNumOfPixels));
            int upperBound=minSideLength<=0?128:(int)Math.min(Math.floor(w/minSideLength),Math.floor(h/minSideLength));
            int initialSize=upperBound;
            if (upperBound<lowerBound){
                initialSize=lowerBound;
            }else if (maxNumOfPixels<=0&&minSideLength<=0){
                initialSize= 1;
            }else if (minSideLength<=0){
                initialSize=lowerBound;
            }
            int inSampleSize;
            if (initialSize<=8){
                inSampleSize=1;
                while (inSampleSize<initialSize){
                    inSampleSize<<=1;
                }
            }else{
                inSampleSize=(initialSize+7)/8*8;
            }
            options.inSampleSize=inSampleSize;
            options.inJustDecodeBounds=false;
            options.inInputShareable=true;
            options.inPurgeable=true;
            return BitmapFactory.decodeFile(filePath,options);
        }catch (Exception e){
            return null;
        }
    }
}
