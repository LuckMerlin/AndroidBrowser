package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.ChunkParser;
import com.luckmerlin.http.Http;
import com.luckmerlin.json.JsonObject;
import org.json.JSONObject;

class CloudFileChunkParser extends ChunkParser<Void,Response<File>>{
    private final OnFileDoingUpdate mOnFileChunkChange;
    private final int mMode;

    public CloudFileChunkParser(int mode,OnFileDoingUpdate chunkChange){
        mMode=mode;
        mOnFileChunkChange=chunkChange;
    }

    @Override
    protected Response<File> onChunkParseFinish(int code1, byte[] thunk, byte[] flag, Http http) {
        try {
            JSONObject jsonObject=parseAsJson(thunk);
            int code=jsonObject.getInt(Label.LABEL_CODE);
            String msg=jsonObject.optString(Label.LABEL_MSG,null);
            Object data=jsonObject.opt(Label.LABEL_DATA);
            if (null==data||!(data instanceof JSONObject)){
                return null;
            }
            return new Response<File>().set(code,msg,new File((JSONObject)data));
        }catch (Exception e){
            Debug.E("Exception parser chunk parse finish.e="+e);
            return null;
        }
    }

    @Override
    protected Void onChunkUpdate(int code1, byte[] thunk, byte[] flag, Http http) {
        try {
            OnFileDoingUpdate onFileChunkChange=mOnFileChunkChange;
            if (null==onFileChunkChange){
                return null;
            }
            JSONObject jsonObject=parseAsJson(thunk);
            int code=jsonObject.getInt(Label.LABEL_CODE);
            String msg=jsonObject.optString(Label.LABEL_MSG,null);
            Object data=jsonObject.opt(Label.LABEL_DATA);
            if (null==data||!(data instanceof JSONObject)){
                return null;
            }
            jsonObject=(JSONObject)data;
            JSONObject fromObj=jsonObject.optJSONObject(Label.LABEL_FROM);
            JSONObject toObj=jsonObject.optJSONObject(Label.LABEL_TO);
            File fromFile=null!=fromObj?new File(fromObj):null;
            File toFile=null!=toObj?new File(toObj):null;
            if (null!=fromFile||null!=toFile){//Progress
                onFileChunkChange.onFileChunkChange(mMode, code==Code.CODE_SUCCEED?100:0, msg,fromFile,toFile);
                return null;//Progress
            }
        }catch (Exception e){
            Debug.E("Exception parser cloud file chunk.e="+e);
            return null;
        }
        return null;
    }

    private JSONObject parseAsJson(byte[] thunk){
        String fromJson=null!=thunk&&thunk.length>0?new String(thunk):null;
        return null!=fromJson? JsonObject.makeJson(fromJson):null;
    }
}