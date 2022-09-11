package com.luckmerlin.browser.client;

import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.http.Answer;
import com.luckmerlin.http.ChunkParser;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.Http;

public abstract class AbstractChunkParser<P,R> extends ChunkParser<P,R> {
    private final static String CUSTOM_CHUNK_FLAG="chunkFlag";

    @Override
    protected final byte[] onResolveChunkFlag(Answer answer, Http http) {
        Headers headers=null!=answer?answer.getHeaders():null;
        String flag= null!=headers?headers.get(CUSTOM_CHUNK_FLAG):null;
        return null!=flag?flag.getBytes():null;
    }
}
