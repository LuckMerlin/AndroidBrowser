package com.luckmerlin.http;

import com.luckmerlin.browser.Code;
import com.luckmerlin.debug.Debug;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class ChunkParser<P,R> implements OnHttpParse<R> {
    private final ChunkFlagResolver mChunkFlagResolver;

    public interface ChunkFlagResolver{
        byte[] onResolveChunkFlag(Answer answer);
    }

    public interface OnChunkParse<T>{
        T onChunkParse(int code,byte[] thunk,byte[] flag,Http http);
    }

    protected abstract P onChunkUpdate(int code,byte[] thunk,byte[] flag,Http http);

    protected abstract R onChunkParseFinish(int code,byte[] thunk,byte[] flag,Http http);

    public ChunkParser(){
        this(null);
    }

    public ChunkParser(ChunkFlagResolver resolver){
        mChunkFlagResolver=resolver;
    }

    @Override
    public R onParse(Http http, Answer answer) {
        if (null==answer){
            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,null,http);
        }
        final Headers headers=answer.getHeaders();
        String encoding=null!=headers?headers.getTransferEncoding():null;
        if (null==encoding||!encoding.equals(Headers.CHUNKED)){
            return onChunkParseFinish(Code.CODE_FAIL,null,null,http);
        }
        AnswerBody answerBody=answer.getAnswerBody();
        InputStream inputStream=null!=answerBody?answerBody.getStream():null;
        if (null==inputStream){
            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,null,http);
        }
        ChunkFlagResolver resolver=mChunkFlagResolver;
        ChunkFinder chunkFinder=null; byte[] thunkFlag=null;
        try {
            byte[] buffer=new byte[1024];int length=0;
            thunkFlag=null!=resolver?resolver.onResolveChunkFlag(answer):null;
            if (null==thunkFlag||thunkFlag.length<=0){
                String thunkFlagString=headers.get("trunkFlag");
                thunkFlag=null!=thunkFlagString&&thunkFlagString.length()>0?thunkFlagString.getBytes():thunkFlag;
                Debug.D("Use http response head chunk flag to read chunk."+thunkFlagString);
            }
            chunkFinder=new ChunkFinder(thunkFlag);
            while ((length=inputStream.read(buffer))>=0){
                if (length>0){
                    chunkFinder.write(buffer,0,length);
                    byte[] chunk=chunkFinder.checkChunk();
                    onChunkUpdate(Code.CODE_CHANGE,chunk,thunkFlag,http);
                }
            }
        }catch (Exception e){
            Debug.E("Exception parse http thunk.e="+e,e);
            e.printStackTrace();
            if (e instanceof EOFException){
                return onChunkParseFinish(Code.CODE_CANCEL,chunkFinder.toByteArray(),thunkFlag,http);
            }
        }
        return onChunkParseFinish(Code.CODE_FINISH,chunkFinder.toByteArray(),thunkFlag,http);
    }

    private static class ChunkFinder extends ByteArrayOutputStream{
        private final byte[] mFlag;
        private int mLastCheckedIndex=0;

        protected ChunkFinder(byte[] flag){
            mFlag=flag;
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
                byte[] chunk=end>0?Arrays.copyOfRange(buf,0,end+1):null;
                System.arraycopy(buf,end,buf,0,count-=index+1);
                mLastCheckedIndex=0;
                return chunk;
            }else{
                mLastCheckedIndex=-index;
            }
            return null;
        }
    }
}
