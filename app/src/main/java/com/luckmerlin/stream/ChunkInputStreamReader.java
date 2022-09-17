package com.luckmerlin.stream;

import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.object.Parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ChunkInputStreamReader extends InputStreamReader{
    private byte[] mChunkFlag;
    private int mMatchedCursor=-1;
    private volatile int mFirstNotMatched=-1;
    private volatile int mReadCursor=-1;
    private boolean mChunked=false;

    public ChunkInputStreamReader(InputStream inputStream, long contentLength,byte[] chunkFlag){
        super(inputStream,contentLength);
        mChunked=false;
        mMatchedCursor=mFirstNotMatched=mReadCursor=-1;
        setChunkFlag(chunkFlag);
    }

    public final InputStreamReader setChunkFlag(byte[] chunkFlag){
        mChunkFlag=null!=chunkFlag&&chunkFlag.length>1?chunkFlag:null;
        return this;
    }

    public final boolean chunked(){
        return mChunked;
    }

    public final int read() throws IOException {
        return onRead();
    }

    public final <R> R readAllChunk(OnChangeUpdate<byte[]> chunkUpdate, Parser<byte[],R> parser2, int size) throws IOException {
        final ByteArrayOutputStream outputStream=new ByteArrayOutputStream(size<=0?1024:size);
        int read;byte[] chunk=null;
        while ((read=read())!=-1){
            outputStream.write(read);
            if (mChunked){
                chunk=outputStream.toByteArray();
                outputStream.reset();
                if (null!=chunkUpdate&&!chunkUpdate.onChangeUpdated(chunk)){
                    break;
                }
            }
        }
        return null!=parser2?parser2.onParse(outputStream.toByteArray()):null;
    }

    protected int onRead() throws IOException {
        mChunked=false;
        InputStream inputStream=mInputStream;
        if (null==inputStream){
            return -1;
        }
        byte[] chunkFlag=mChunkFlag;
        if (null==chunkFlag){
            return inputStream.read();
        }
        int read=-1;
        if (mReadCursor>=0&&mMatchedCursor>=mReadCursor){//Exist matched
            read= chunkFlag[mReadCursor++];
            if (mMatchedCursor<mReadCursor){//Matched all read
                mReadCursor=mMatchedCursor=-1;
            }
            return read;
        }else if(mFirstNotMatched!=-1){
            read=mFirstNotMatched;
            mFirstNotMatched=-1;
            return read;
        }
        if ((read=inputStream.read())==-1){//Read finish
            mReadCursor=mMatchedCursor>=0?0:mReadCursor;
            return read;
        }
        if (chunkFlag[mMatchedCursor<0?0:mMatchedCursor+1]==read){
            if (((mMatchedCursor=(mMatchedCursor<0?0:mMatchedCursor+1))==chunkFlag.length-1)){//End chunk?
                mMatchedCursor=mReadCursor=mFirstNotMatched=-1;
                mChunked=true;
            }
            return read();//Reread
        }else if (mMatchedCursor<0){//Not matched,directly return
            return read;
        }
        mFirstNotMatched=read;
        mReadCursor=0;
        return read();
    }
}
