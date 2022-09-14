package com.luckmerlin.browser.client;

import com.luckmerlin.core.OnChangeUpdate;

public class DoingFileChunkUpdateParser implements OnChangeUpdate<byte[]> {
    private OnFileDoingUpdate mOnFileDoingUpdate;
    private final int mMode;

    public DoingFileChunkUpdateParser(int mode,OnFileDoingUpdate onFileDoingUpdate){
        mMode=mode;
        mOnFileDoingUpdate=onFileDoingUpdate;
    }

    @Override
    public boolean onChangeUpdated(byte[] newData) {
        return false;
    }
}
