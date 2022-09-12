package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.object.Parser;
import org.json.JSONException;
import org.json.JSONObject;

public class DoingFileUpdateParser implements Parser<byte[], Void> {
    private final OnFileDoingUpdate mOnFileDoingUpdate;
    private final int mMode;

    public DoingFileUpdateParser(int mode,OnFileDoingUpdate onFileDoingUpdate){
        mOnFileDoingUpdate=onFileDoingUpdate;
        mMode=mode;
    }

    @Override
    public Void onParse(byte[] from) {
        OnFileDoingUpdate  onFileDoingUpdate=mOnFileDoingUpdate;
        if (null==onFileDoingUpdate){
            return null;
        }
        JSONObject jsonObject=JsonObject.makeJson(from);
        if (null==jsonObject){
            return null;
        }
        try {
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
                onFileDoingUpdate.onFileChunkChange(mMode, code== Code.CODE_SUCCEED?100:0, msg,fromFile,toFile);
                return null;//Progress
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
