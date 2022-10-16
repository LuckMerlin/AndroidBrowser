package com.luckmerlin.browser.client;

import com.luckmerlin.http.Answer;
import com.luckmerlin.http.AnswerBody;
import com.luckmerlin.http.Connection;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.Requested;
import com.luckmerlin.stream.ChunkInputStreamReader;

public class AnswerChunkInputStreamReader extends ChunkInputStreamReader {
    private static final String CUSTOM_CONTENT_LENGTH_KEY="MerlinContentLength";
    private final static String CUSTOM_CHUNK_FLAG="chunkFlag";
    private Connection nConnection;

    public AnswerChunkInputStreamReader(Connection connection) {
        super(null,0,null);
        nConnection=connection;
        Requested requested=null!=connection?connection.getRequested():null;
        Answer answer=null!=requested?requested.getAnswer():null;
        AnswerBody answerBody=null!=answer?answer.getAnswerBody():null;
        Headers headers=null!=answer?answer.getHeaders():null;
        long contentLength=answerBody.getContentLength();
        contentLength=contentLength<0?headers.getLong(CUSTOM_CONTENT_LENGTH_KEY,-1):contentLength;
        setInputStream(null!=answerBody?answerBody.getInputStream():null,contentLength);
        String flag= null!=headers?headers.get(CUSTOM_CHUNK_FLAG):null;
        setChunkFlag(null!=flag?flag.getBytes():null);

    }

    public Connection getConnection() {
        return nConnection;
    }
}
