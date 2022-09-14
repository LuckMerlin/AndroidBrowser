package com.luckmerlin.stream;

import com.luckmerlin.browser.client.ChunkFileInputStream;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.object.Parser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChunkInputStreamReader {
    private InputStream mInputStream;
    private byte[] mChunkFlag;
    private int mMatchedCursor=-1;
    private volatile int mFirstNotMatched=-1;
    private volatile int mReadCursor=-1;
    private boolean mChunked=false;

    public ChunkInputStreamReader(){
        this(null,null);
    }

    public ChunkInputStreamReader(InputStream inputStream, byte[] chunkFlag){
        setInputStream(inputStream);
        mChunked=false;
        mMatchedCursor=mFirstNotMatched=mReadCursor=-1;
        setChunkFlag(chunkFlag);
    }

    public final ChunkInputStreamReader setInputStream(InputStream inputStream){
        mInputStream=inputStream;
        return this;
    }

    public final ChunkInputStreamReader setChunkFlag(byte[] chunkFlag){
        mChunkFlag=null!=chunkFlag&&chunkFlag.length>1?chunkFlag:null;
        return this;
    }

    public final boolean chunked(){
        return mChunked;
    }

    public final <R> R read(OnChangeUpdate<byte[]> chunkUpdate, Parser<byte[],R> parser2, int size) throws IOException {
        final ByteArrayOutputStream outputStream=new ByteArrayOutputStream(size<=0?1024:size);
        int read;
        while ((read=read())!=-1){
            outputStream.write(read);
            if (mChunked){
                if (null!=chunkUpdate&&chunkUpdate.onChangeUpdated(outputStream.toByteArray())){
                    continue;
                }
                outputStream.reset();
            }
        }
        return null!=parser2?parser2.onParse(outputStream.toByteArray()):null;
    }

    private boolean write(OutputStream outputStream,byte[] bytes){
        if (null==outputStream||null==bytes){
            return false;
        }
        try {
            if (null!=bytes&&bytes.length>0){
                outputStream.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public final boolean pipe(OutputStream outputStream) throws IOException {
        if (null==outputStream){
            return false;
        }
        read((byte bytes[])->write(outputStream,bytes),(byte[] bytes)->write(outputStream,bytes),1024);
        return true;
    }

    public int read(byte buffer[]) throws IOException {
        return read(buffer, 0, null!=buffer?buffer.length:0);
    }

    public final int read(byte[] buffer,int offset,int size) throws IOException {
        if (buffer == null) {
            throw new NullPointerException();
        } else if (offset < 0 || size < 0 || size > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        } else if (size == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        buffer[offset] = (byte)c;

        int i = 1;
        try {
            for (; i < size ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                buffer[offset + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

    public final int read() throws IOException {
        return onRead();
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
