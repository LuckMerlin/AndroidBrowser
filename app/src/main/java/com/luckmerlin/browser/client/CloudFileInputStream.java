package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Utils;
import com.luckmerlin.stream.InputStream;

import java.io.IOException;

public class CloudFileInputStream<T> extends InputStream {
    private final AnswerChunkInputStreamReader mReader;
    private long mContentLength=-1;

    public CloudFileInputStream(AnswerChunkInputStreamReader reader) {
        super(null==reader?-1:reader.getContentLength());
        mContentLength=-1;
        mReader=reader;
    }

    @Override
    public long length() {
        if (mContentLength>=0){
            return mContentLength;
        }
        AnswerChunkInputStreamReader reader=mReader;
        return mContentLength=null!=reader?reader.getContentLength():0;
    }

    @Override
    public int onRead(byte[] b, int off, int len) throws IOException {
        AnswerChunkInputStreamReader reader=mReader;
        return null!=reader?reader.read(b,off,len):-1;
    }

    @Override
    public void close() throws IOException {
        AnswerChunkInputStreamReader reader=mReader;
        Utils.closeStream(null!=reader?reader.getConnection():null);
    }
}
