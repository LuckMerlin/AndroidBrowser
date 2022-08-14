package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;

import java.nio.file.Path;


public class AndroidFileStream {

    public File[] listFiles(File file){
        String path=null!=file?file.getPath():null;
        if (null==path||path.length()<=0){
            return null;
        }
        return null;
    }

//    public void copyFile(File from,File to){
//        //sdcard/a /sdcard/b
//        //sdcard/a/c  /sdcard/b/c
//        copyFile(from,to,null);
//    }

    public Response<File> createFolder(File to){
        return null;
    }

//    private Response<File> copyFile(File from,File to){
//        if (null==from||null==to){
//            return new Response<File>().set(Code.CODE_FAIL, "From or to file invalid.",null);
//        }else if (from.isDirectory()){//Is folder
//            Response<File> response=createFolder(to);
//            response=null!=response?response:new Response<File>().set(Code.CODE_FAIL,
//                    "Create folder fail."+to.getPath(),null);
//            if (null!=response&&!response.isSucceed()){
//                return response;
//            }
//            //Create folder succeed
//            File[] files=listFiles(from);
//            Response<File> childResponse=null;
//            if (null!=files&&files.length>0){
//                for (File child:files) {
//                    if (null==child){
//                        continue;
//                    }
//                    childResponse=copyFile(child,);
//                    childResponse=null!=childResponse?childResponse:new Response<File>().
//                            set(Code.CODE_FAIL,"Unknown error.");
//                    if (!childResponse.isSucceed()){
//                        break;
//                    }
//                }
//            }
//            return childResponse;
//        }
//        return null;
//    }

//    public boolean copyFile(File file){
//        if (null==file) {
//            return false;
//        }
//        OutputStream outputStream=null;
//        if (!file.isDirectory()){//文件
////            new FileInputStream();
//            new InputStream();
//            return false;
//        }
//        File[] files=file.listFiles();
//        if (null==files||files.length<=0){
//            return false;
//        }
//        for (File child:files) {
//            copyFile(child);
//        }
//        return true;
//    }
}
