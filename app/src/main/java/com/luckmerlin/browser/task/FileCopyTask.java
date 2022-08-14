package com.luckmerlin.browser.task;

import android.content.Context;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.stream.StreamCopyTask;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskProgress;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileCopyTask extends FileTask {
    private final File mFromFile;
    private final File mToFile;
    private byte[] mBuffer;

    public FileCopyTask(File fromFile,File toFile,Progress progress) {
        super(progress);
        mFromFile=fromFile;
        mToFile=toFile;
    }

    private Response<File> createFolder(File file) throws Exception {
        String path=null!=file?file.getPath():null;
        if (null==path){
            Debug.W("Fail create folder while file path invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"File path invalid");
        }
        if (file.isLocalFile()){
            java.io.File androidFile=new java.io.File(path);
            if (androidFile.exists()){
                if (!androidFile.isDirectory()){
                    Debug.W("Fail create folder while file already exist but not directory."+path);
                    return new Response(Code.CODE_ERROR,"File already exist but not directory");
                }
            }else if (androidFile.mkdirs()){
                Debug.D("Created android folder."+path);
            }
            File newFolder=androidFile.exists()? LocalClient.createLocalFile(androidFile):null;
            if (null==newFolder){
                Debug.W("Fail create android folder."+path);
                return new Response(Code.CODE_ERROR,"File create android folder."+path);
            }
            return new Response(Code.CODE_SUCCEED,"Succeed.",newFolder);
        }
        return null;
    }

    private Response<List<File>> listFiles(File file){
        String path=null!=file?file.getPath():null;
        if (null==path){
            Debug.W("Fail list files while file path invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"File path invalid");
        }
        if (file.isLocalFile()){
            java.io.File androidFile=new java.io.File(path);
            List<File> files=new ArrayList<>();
            Response<List<File>> response=new Response<List<File>>().set(Code.CODE_SUCCEED,null,files);
            java.io.File[] localFiles=androidFile.listFiles();
            if (null!=localFiles&&localFiles.length>0){
                for (java.io.File child:localFiles) {
                    File createFile=LocalClient.createLocalFile(child);
                    if (null==createFile){
                        Debug.W("Fail list files while child path create invalid."+child);
                        return new Response(Code.CODE_ARGS_INVALID,"Child path create invalid."+child);
                    }
                    files.add(createFile);
                }
            }
            return response;
        }
        return null;
    }

    private Response<OutputStream> openOutputStream(File file) throws Exception{
        String path=null!=file?file.getPath():null;
        if (null==path){
            Debug.W("Fail open file output stream while file path invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"File path invalid");
        }
        if (file.isLocalFile()) {
            java.io.File androidFile = new java.io.File(path);
            if (androidFile.isDirectory()){
                Debug.W("Fail open file output stream while file is android directory.");
                return new Response(Code.CODE_ARGS_INVALID,"File is android directory");
            }
            FileOutputStream fileOutputStream=new FileOutputStream(androidFile,true);
            final OutputStream outputStream=new OutputStream(androidFile.length(),null){
                @Override
                public void close() throws IOException {
                    fileOutputStream.close();
                }

                @Override
                protected void onWrite(int b) throws IOException {
                    fileOutputStream.write(b);
                }
            };
            outputStream.setTitle(androidFile.getName());
            return new Response<OutputStream>().set(Code.CODE_SUCCEED,"Succeed.",outputStream);
        }
        return null;
    }

    private Response<InputStream> openInputStream(long openLength,File file)throws Exception{
        String path=null!=file?file.getPath():null;
        if (null==path){
            Debug.W("Fail open file input stream while file path invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"File path invalid");
        }
        if (file.isLocalFile()) {
            java.io.File androidFile = new java.io.File(path);
            if (androidFile.isDirectory()){
                Debug.W("Fail open file input stream while file is android directory.");
                return new Response(Code.CODE_ARGS_INVALID,"File is android directory");
            }
            FileInputStream fileInputStream=new FileInputStream(androidFile);
            if (openLength<0){
                Debug.W("Fail open file input stream while file open length invalid.");
                return new Response(Code.CODE_ARGS_INVALID,"File open length invalid");
            }else if (openLength>0){
                Debug.D("Open file input stream with skip."+openLength);
                fileInputStream.skip(openLength);
            }
            final InputStream inputStream=new InputStream(openLength,null){

                @Override
                public void close() throws IOException {
                    fileInputStream.close();
                }

                @Override
                public long length() {
                    return androidFile.length();
                }

                @Override
                protected int onRead() throws IOException {
                    return fileInputStream.read();
                }
            };
            inputStream.setTitle(androidFile.getName());
            return new Response<InputStream>().set(Code.CODE_SUCCEED,"Succeed.",inputStream);
        }
        return null;
    }

    @Override
    protected Result onExecute() {
        File file=mFromFile;
        TaskProgress progress=new TaskProgress().setPosition(0).setTotal(1).setTitle(null!=file?file.getName():null);
        notifyProgress(progress);
        Result result=copyFile(mFromFile, mToFile, (File fromFile, File toFile, Progress childProgress)->
                notifyProgress(progress.setSubProgress(childProgress)));
        notifyProgress(progress.setPosition(1));
        return result;
    }

    private Result copyFile(File fromFile,File toFile,OnFileProgress onProgressChange){
        final String fromPath=null!=fromFile?fromFile.getPath():null;
        if (null==fromPath||fromPath.length()<=0){
            Debug.W("Fail execute file copy task while from file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"From file invalid");
        }
        String toPath=null!=toFile?toFile.getPath():null;
        if (null==toPath||toPath.length()<=0){
            Debug.W("Fail execute file copy task while to file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"To file invalid");
        }
        OutputStream outputStream=null;InputStream inputStream=null;
        try {
            if (null!=onProgressChange){
                onProgressChange.onFileProgressChange(fromFile,toFile,new TaskProgress().setTitle(fromFile.getName()));
            }
            if (fromFile.isDirectory()){//Is folder
                Response<File> response=createFolder(toFile);
                response=null!=response?response:new Response<File>().set(Code.CODE_FAIL,
                        "Create folder fail."+toFile.getPath(),null);
                if (null!=response&&!response.isSucceed()){
                    Debug.W("Fail execute file copy task while create directory fail.");
                    return response;
                }
                //Create folder succeed
                Response<List<File>> listFilesResponse=listFiles(fromFile);
                if (null==listFilesResponse){
                    Debug.W("Fail copy file while list directory fail.path="+fromPath);
                    return new Response<File>().set(Code.CODE_FAIL, "List directory fail."+fromPath);
                }else if (!listFilesResponse.isSucceed()){
                    Debug.W("Fail copy file while list directory fail."+listFilesResponse.getMessage()+".path="+fromPath);
                    return new Response<File>().set(listFilesResponse.getCode(Code.CODE_FAIL), listFilesResponse.getMessage());
                }
                List<File> files=listFilesResponse.getData();
                //List files
                Result childResponse=new Response<>(Code.CODE_SUCCEED,"Empty",response.getData());
                if (null!=files&&files.size()>0){
                    for (File child:files) {
                        if (null==child){
                            continue;
                        }
                        childResponse=copyFile(child,new File().setHost(child.getHost()).setName(child.getName()).
                                setSep(child.getSep()).setParent(toPath),onProgressChange);
                        childResponse=null!=childResponse?childResponse:new Response<File>().set(Code.CODE_FAIL,"Unknown error.");
                        if (!(childResponse instanceof Response)||!((Response)childResponse).isSucceed()){
                            if (childResponse instanceof ConfirmResult){
                                Debug.W("Children copy need confirm.path="+fromPath);
                            }else{
                                Debug.W("Fail copy file while children copy fail.path="+fromPath);
                            }
                            break;
                        }
                    }
                }
                return childResponse;
            }
            Debug.D("To open file copy output stream."+toPath);
            Response<OutputStream> childOutputResponse=openOutputStream(toFile);
            outputStream=null!=childOutputResponse?childOutputResponse.getData():null;
            if (null==outputStream){
                Debug.W("Fail execute file copy task while open output stream fail.");
                return new Response(Code.CODE_FAIL,"Open output stream fail");
            }else if (!childOutputResponse.isSucceed()){
                Debug.W("Fail execute file copy task while open output stream fail.");
                return new Response(childOutputResponse.getCode(Code.CODE_FAIL),childOutputResponse.getMessage());
            }
            Debug.D("Succeed open file copy output stream."+toPath);
            final long outputOpenLength=outputStream.getOpenLength();
            //
            Debug.D("To open file copy input stream."+fromPath);
            Response<InputStream> childInputResponse=openInputStream(outputOpenLength,fromFile);
            inputStream=null!=childInputResponse?childInputResponse.getData():null;
            if (null==inputStream){
                Debug.W("Fail execute file copy task while open input stream fail.");
                return new Response(Code.CODE_FAIL,"Open input stream fail");
            }else if (!childInputResponse.isSucceed()){
                Debug.W("Fail execute file copy task while open input stream fail.");
                return new Response(childInputResponse.getCode(Code.CODE_FAIL),childInputResponse.getMessage());
            }
            if (outputOpenLength>0){
                if (inputStream.getTotal()==outputOpenLength){
                    Debug.W("Not need execute file copy task while already done.");
                    return new Response(Code.CODE_ALREADY,"Already done.");
                }else if (isConfirmEnabled()){//Need confirm
                    return new ConfirmResult() {
                        @Override
                        protected Confirm onCreate(Context context) {
                            return new Confirm().setMessage(getString(context, R.string.areYourSureWhich,
                                    getString(context,R.string.replace)+" "+toFile.getName())+
                                    "\n"+toPath).setOnConfirm((boolean confirm)->
                                    null!=enableConfirm(!confirm)?FileCopyTask.this:FileCopyTask.this);
                        }
                    };
                }
            }
            byte[] buffer=mBuffer;
            buffer=null!=buffer?buffer:(mBuffer=new byte[1024]);
            return new StreamCopyTask(inputStream,outputStream,buffer,null).setName(fromFile.getName()).
                    execute(null!=onProgressChange?(Task task, Progress progress)->
                            onProgressChange.onFileProgressChange(fromFile,toFile,progress):null);
        }catch (Exception e){
            Debug.W("Exception execute file copy task.e="+e);
            return new Response(Code.CODE_ERROR,"Exception execute file copy task.fromPath="+fromPath+" toPath="+toPath);
        }
    }

    private interface OnFileProgress{
        void onFileProgressChange(File fromFile,File toFile,Progress progress);
    }
}
