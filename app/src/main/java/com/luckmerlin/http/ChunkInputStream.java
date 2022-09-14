package com.luckmerlin.http;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.object.Parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ChunkInputStream extends InputStream {
    private InputStream mInputStream;
    private ChunkFinder mChunkFinder;
    private int mChunkReadCursor=-1;
    private byte[] mChunk;
    private final byte[] mBuffer=new byte[1024];
    private OnChunkCheck mOnChunkCheck;

    public interface OnChunkCheck{
        boolean onChunkChecked(byte[] chunk);
    }

    public ChunkInputStream(InputStream inputStream,byte[] chunkFlag){
        mChunkReadCursor=-1;
        mChunk=null;
        setInputStream(inputStream);
        setChunkFlag(chunkFlag);
    }

    protected final ChunkInputStream setInputStream(InputStream inputStream){
        mInputStream=inputStream;
        return this;
    }

    protected final ChunkInputStream setChunkFlag(byte[] chunkFlag){
        mChunkFinder=null!=chunkFlag&&chunkFlag.length>0?new ChunkFinder(chunkFlag):null;
        return this;
    }

    public final ChunkInputStream setOnChunkCheck(OnChunkCheck onChunkCheck){
        if (null==mOnChunkCheck){
            mOnChunkCheck=onChunkCheck;
        }
        return this;
    }

    public final <T> T read(Parser<byte[],?> onChunkCheck, Parser<byte[],T> resultConverter){
        setOnChunkCheck((byte[] chunk)->(null!=onChunkCheck&&null!=onChunkCheck.onParse(chunk))||true);
        try {
            read();
            if (null==resultConverter){
                return null;
            }
            ChunkFinder chunkFinder=mChunkFinder;
            return resultConverter.onParse(null!=chunkFinder?chunkFinder.toByteArray():null);
        } catch (IOException e) {
            Debug.E("Exception read chunk input stream.e="+e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int read() throws IOException {
        InputStream inputStream=mInputStream;
        if (null==inputStream){
            Debug.E("Fail read chunk input stream while input stream invalid.");
            return -1;
        }
        ChunkFinder chunkFinder=mChunkFinder;
        if (null==chunkFinder){
            return inputStream.read();
        }else if (mChunkReadCursor>=0&&null!=mChunk&&mChunkReadCursor<mChunk.length){
            return mChunk[mChunkReadCursor++];
        }
        int read=0;mChunkReadCursor=-1;mChunk=null;
        OnChunkCheck onChunkCheck=mOnChunkCheck;
        while ((read=inputStream.read(mBuffer))>=0){
            if (read>0){
                chunkFinder.write(mBuffer,0,read);
                if (null!=(mChunk=chunkFinder.checkChunk())&&
                        (null==onChunkCheck||!onChunkCheck.onChunkChecked(mChunk))){
                    mChunkReadCursor=0;
                    break;
                }
            }
        }
        return (null==mChunk||mChunkReadCursor!=0)?-1:read();
    }

    private static class ChunkFinder extends ByteArrayOutputStream {
        protected final byte[] mFlag;
        private int mLastCheckedIndex=0;

        protected ChunkFinder(byte[] flag){
            mFlag=flag;
            //34
            ///123456789
        }

        private int index(int offset,byte[] target){
            int length=null!=target?target.length:-1;
            if (length>0&&null!=buf){
                int j=0;int start=0;
                for (int i = offset; i < count; i++) {
                    for (j = 0; j < length; j++) {
                        if ((start=i+j)>=count){
                            return -i;
                        }else if (buf[start]!=target[j]){
                            start=0;
                            break;
                        }
                    }
                    if (start<=0){
                        continue;
                    }
                    return start;
                }
            }
            return 0;
        }

        public byte[] checkChunk(){
            byte[] flag=mFlag;
            if (null==buf||buf.length<=0||count<=0||buf.length<count||null==flag||flag.length<=0){
                return null;
            }
            int index=index(mLastCheckedIndex,flag);
            if(index>0){
                int end=index-flag.length;
                byte[] chunk=null;
                if (end>0){
                    chunk=Arrays.copyOfRange(buf,0,end+1);
                    System.arraycopy(buf,end,buf,0,count-=index+1);
                    mLastCheckedIndex=0;
                }
                return chunk;
            }else{
                mLastCheckedIndex=-index;
            }
            return null;
        }
    }
}
