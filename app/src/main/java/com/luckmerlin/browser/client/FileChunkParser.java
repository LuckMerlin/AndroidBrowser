//package com.luckmerlin.browser.client;
//
//import com.luckmerlin.core.Code;
//import com.luckmerlin.browser.Label;
//import com.luckmerlin.browser.file.File;
//import com.luckmerlin.core.Response;
//import com.luckmerlin.debug.Debug;
//import com.luckmerlin.http.Answer;
//import com.luckmerlin.http.AnswerBody;
//import com.luckmerlin.http.ChunkParser;
//import com.luckmerlin.http.Headers;
//import com.luckmerlin.http.Http;
//import com.luckmerlin.json.JsonObject;
//import org.json.JSONObject;
//
//import java.io.InputStream;
//
//class FileChunkParser extends AbstractChunkParser<Void,Response<File>>{
//    private final OnFileDoingUpdate mOnFileChunkChange;
//    private final int mMode;
//
//    public FileChunkParser(int mode, OnFileDoingUpdate chunkChange){
//        mMode=mode;
//        mOnFileChunkChange=chunkChange;
//    }
//
//    @Override
//    protected Response<File> onChunkParseFinish(int code1, byte[] thunk, byte[] flag, Http http) {
//        try {
//            JSONObject jsonObject=parseAsJson(thunk);
//            if (null==jsonObject){
//                return new Response<>(code1,"Chunk json invalid.",null);
//            }
//            int code=jsonObject.getInt(Label.LABEL_CODE);
//            String msg=jsonObject.optString(Label.LABEL_MSG,null);
//            Object data=jsonObject.opt(Label.LABEL_DATA);
//            if (null==data||!(data instanceof JSONObject)){
//                return new Response<>(code,msg,null);
//            }
//            return new Response<File>().set(code,msg,new File((JSONObject)data));
//        }catch (Exception e){
//            Debug.E("Exception parser chunk parse finish.e="+e);
//            return new Response<>(code1,"Chunk json parse exception."+e,null);
//        }
//    }
//
//    @Override
//    protected Response<File> onReadChunk(ChunkFinder chunkFinder, byte[] chunkFlag, Answer answer, Http http) throws Exception {
//        AnswerBody answerBody=null!=answer?answer.getAnswerBody():null;
//        java.io.InputStream inputStream=null!=answerBody?answerBody.getInputStream():null;
//        if (null==chunkFinder){
//            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,chunkFlag,http);
//        }
//        if (null==inputStream){
//            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,chunkFlag,http);
//        }
//        byte[] buffer=new byte[1024];int length=0;
//        while ((length=inputStream.read(buffer))>=0){
//            if (length>0){
//                chunkFinder.write(buffer,0,length);
//                onChunkUpdate(chunkFinder.checkChunk());
//            }
//        }
//        return onChunkParseFinish(Code.CODE_SUCCEED,chunkFinder.toByteArray(),chunkFlag,http);
//    }
//
//    private Void onChunkUpdate(byte[] thunk) {
//        try {
//            OnFileDoingUpdate onFileChunkChange=mOnFileChunkChange;
//            if (null==onFileChunkChange){
//                return null;
//            }
//            JSONObject jsonObject=parseAsJson(thunk);
//            if (null==jsonObject){
//                return null;
//            }
//            int code=jsonObject.getInt(Label.LABEL_CODE);
//            String msg=jsonObject.optString(Label.LABEL_MSG,null);
//            Object data=jsonObject.opt(Label.LABEL_DATA);
//            if (null==data||!(data instanceof JSONObject)){
//                return null;
//            }
//            jsonObject=(JSONObject)data;
//            JSONObject fromObj=jsonObject.optJSONObject(Label.LABEL_FROM);
//            JSONObject toObj=jsonObject.optJSONObject(Label.LABEL_TO);
//            File fromFile=null!=fromObj?new File(fromObj):null;
//            File toFile=null!=toObj?new File(toObj):null;
//            if (null!=fromFile||null!=toFile){//Progress
//                onFileChunkChange.onFileChunkChange(mMode, code==Code.CODE_SUCCEED?100:0, msg,fromFile,toFile);
//                return null;//Progress
//            }
//        }catch (Exception e){
//            Debug.E("Exception parser cloud file chunk.e="+e);
//            return null;
//        }
//        return null;
//    }
//
//    protected final JSONObject parseAsJson(byte[] thunk){
//        String fromJson=null!=thunk&&thunk.length>0?new String(thunk):null;
//        return null!=fromJson? JsonObject.makeJson(fromJson):null;
//    }
//}