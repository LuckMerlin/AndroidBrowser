//package com.luckmerlin.browser.task;
//
//import android.content.Context;
//import com.luckmerlin.browser.Client;
//import com.luckmerlin.core.Code;
//import com.luckmerlin.browser.R;
//import com.luckmerlin.browser.file.DoingFiles;
//import com.luckmerlin.browser.file.File;
//import com.luckmerlin.core.Response;
//import com.luckmerlin.core.Result;
//import com.luckmerlin.debug.Debug;
//import com.luckmerlin.task.ConfirmResult1;
//import com.luckmerlin.task.Executor;
//import com.luckmerlin.task.OnProgressChange;
//import com.luckmerlin.task.Progress;
//import com.luckmerlin.task.Runtime;
//
//public class FileDeleteTask extends FileTask {
//    private File mFile;
//
//    public FileDeleteTask(File file, Progress progress) {
//        super(progress);
//        mFile=file;
//        setName(null!=file?file.getName():null);
//    }
//
//    @Override
//    protected Result onExecute(Runtime runtime) {
//        File file=mFile;
//        if (null==file){
//            Debug.W("Fail execute file delete task while arg invalid.");
//            return new Response(Code.CODE_ARGS_INVALID,"Delete arg invalid.");
//        }
//        Client client=getFileClient(file);
//        if (null==client){
//            Debug.W("Fail execute file delete task while client invalid.");
//            return new Response(Code.CODE_ERROR,"Client invalid.");
//        }
////
////        if (isConfirmEnabled()){
////            Debug.D("Make execute delete android file confirm.");
////            Executor executor=null!=runtime?runtime.getExecutor():null;
////            OnProgressChange onProgressChange=getOnProgressChange();
////            return null!=executor?new ConfirmResult1() {
////                @Override
////                protected Confirm onCreate(Context context) {
////                    String delete=""+getString(context,R.string.delete);
////                    return new ConfirmResult1.Confirm(getString(context, R.string.confirmWhich,
////                            delete+(getString(context,file.isDirectory()?R.string.folder:R.string.file)) +"["+file.getName()+"]"), (boolean confirm)->
////                            confirm&&executor.execute(FileDeleteTask.this,runtime.enableConfirm(false).getOption(),onProgressChange)?null:null
////                    ).setTitle(delete);
////                }
////            }:null;
////        }
//        Progress progress=new Progress().setTotal(1).setPosition(0);
//        notifyProgress(progress);
//        DoingFiles doingFiles=new DoingFiles();
//        progress.setData(doingFiles);
//        Response response= client.deleteFile(file,(int mode,int pro, String msg, File from, File to)-> {
//            doingFiles.setProgress(pro).setDoingMode(mode).setFrom(from).setTo(to);
//            runtime.post(()->{
//                doingFiles.setProgress(pro).setDoingMode(mode).setFrom(from).setTo(to);
//                notifyProgress(progress);
//            },0);
//            return isCancelEnabled();
//        });
//        if (null!=response&&response.isSucceed()){
//            notifyProgress(progress.setPosition(1));
//        }
//        return response;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o){
//            return true;
//        }else if (!(o instanceof FileDeleteTask)){
//            return false;
//        }
//        File file=mFile;
//        return ((null==file&&null==mFile)||(null!=file&&null!=mFile&&file.equals(mFile)));
//    }
//}
