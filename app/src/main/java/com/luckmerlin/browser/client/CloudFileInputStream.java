package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Utils;
import com.luckmerlin.http.ChunkInputStream;
import com.luckmerlin.http.Connection;
import com.luckmerlin.stream.InputStream;
import java.io.IOException;

public class CloudFileInputStream extends InputStream {
    private final ChunkFileInputStream mChunkInputStream;

    public CloudFileInputStream(long openLength,Connection connection) {
        super(openLength, null);
        mChunkInputStream=null!=connection?new ChunkFileInputStream(connection):null;
    }

    @Override
    public long length() {
        ChunkFileInputStream inputStream=mChunkInputStream;
        return null!=inputStream?inputStream.getContentLength():0;
    }

    @Override
    protected int onRead() throws IOException {
        ChunkInputStream inputStream=mChunkInputStream;
        return null!=inputStream?inputStream.read():-1;
    }

    @Override
    public void close() throws IOException {
        Utils.closeStream(mChunkInputStream);
    }
}
