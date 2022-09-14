package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Utils;
import com.luckmerlin.stream.InputStream;
import java.io.IOException;

public class CloudFileInputStream extends InputStream {
    private final AnswerChunkInputStreamReader mReader;

    public CloudFileInputStream(AnswerChunkInputStreamReader reader) {
        super(null==reader?-1:reader.getContentLength(), null);
        mReader=reader;
    }

    @Override
    public long length() {
        AnswerChunkInputStreamReader reader=mReader;
        return null!=reader?reader.getContentLength():0;
    }

    @Override
    protected int onRead() throws IOException {
        AnswerChunkInputStreamReader reader=mReader;
        return null!=reader?reader.read():-1;
    }

    @Override
    public void close() throws IOException {
        AnswerChunkInputStreamReader reader=mReader;
        Utils.closeStream(null!=reader?reader.getConnection():null);
    }
}
