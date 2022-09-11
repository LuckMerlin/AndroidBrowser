package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Code;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.Answer;
import com.luckmerlin.http.AnswerBody;
import com.luckmerlin.http.ChunkParser;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.Http;
import com.luckmerlin.stream.InputStream;
import java.io.IOException;

public class EncryptFileChunkParser extends AbstractChunkParser<Void, Response<InputStream>> {
    private static final String CUSTOM_RANGE_START_KEY="MerlinRangeStart";
    private static final String CUSTOM_CONTENT_LENGTH_KEY="MerlinContentLength";

    @Override
    protected Response<InputStream> onChunkParseFinish(int code, byte[] thunk, byte[] flag, Http http) {
        return new Response<InputStream>().set(code,"Default chunk parse.",null);
    }

    @Override
    protected Response<InputStream> onReadChunk(ChunkParser.
            ChunkFinder chunkFinder, byte[] chunkFlag, Answer answer, Http http) throws Exception {
        AnswerBody answerBody=null!=answer?answer.getAnswerBody():null;
        java.io.InputStream inputStream=null!=answerBody?answerBody.getStream():null;
        if (null==chunkFinder){
            Debug.E("Fail read file chunk while chunk finder invalid.");
            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,chunkFlag,http);
        }
        if (null==inputStream){
            Debug.E("Fail read file chunk while input stream invalid.");
            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,chunkFlag,http);
        }
        Headers headers=null!=answer?answer.getHeaders():null;
        if (null==headers){
            Debug.E("Fail read file chunk while http headers invalid.");
            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,chunkFlag,http);
        }
        long contentLength=answerBody.getContentLength();
        final long finalContentLength=contentLength<0?headers.
                getLong(CUSTOM_CONTENT_LENGTH_KEY,-1):contentLength;
        if (finalContentLength<0){
            Debug.E("Fail read file chunk while content length invalid.");
            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,chunkFlag,http);
        }
        long rangeStart=headers.getContentRangeStart(-1);
        rangeStart=rangeStart>=0?rangeStart:headers.getLong(CUSTOM_RANGE_START_KEY,-1);
        if (rangeStart<0){
            Debug.E("Fail read file chunk while range start invalid."+rangeStart);
            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,chunkFlag,http);
        }
        final byte[] buffer=new byte[1024];
        final Reader reader=new Reader(){
            @Override
            protected byte[] onFillBytes() throws IOException{
                int length=0;byte[] chunk=null;
                while ((length=inputStream.read(buffer))>=0){
                    if (length>0){
                        chunkFinder.write(buffer,0,length);
                        Debug.D("SFDSAFDSADFA length="+length+" "+new String(chunkFlag));
                        if (null==(chunk=chunkFinder.checkChunk())){
                            continue;//Circle to again to until read one chunk
                        }
                        Debug.D("SFDSAFDSADFA "+chunk);
                        break;//We just need read one chunk each time
                    }
                }
                return chunk;
            }
        };
        return new Response<>(Code.CODE_SUCCEED, "Succeed",
                new InputStream(rangeStart,null){

                    @Override
                    public void close() throws IOException {
                        inputStream.close();
                    }

                    @Override
                    public long length() {
                        return finalContentLength;
                    }

                    @Override
                    protected int onRead()  throws IOException {
                        return reader.readByte();
                    }
                });
    }

    private static abstract class Reader{
        private byte[] mBytes;
        private int mReadCursor;

        protected abstract byte[] onFillBytes() throws IOException;

        public final int readByte() throws IOException{
            byte[] bytes=mBytes;
            int cursor=mReadCursor;
            if (null==bytes||cursor>=bytes.length){ //All read
                mReadCursor=0;
                mBytes=bytes=onFillBytes();
                return null!=bytes&&bytes.length>0?readByte():-1;
            }
            mReadCursor++;
            return bytes[cursor];
        }
    }

}
