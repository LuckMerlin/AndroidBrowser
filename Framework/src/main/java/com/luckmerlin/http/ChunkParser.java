package com.luckmerlin.http;

import com.luckmerlin.core.Code;
import com.luckmerlin.debug.Debug;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.util.Arrays;

public abstract class ChunkParser<R> implements OnHttpParse<R> {
    private final ChunkFlagResolver mChunkFlagResolver;

    public interface ChunkFlagResolver{
        byte[] onResolveChunkFlag(Answer answer);
    }

    protected abstract R onChunkParseFinish(int code,byte[] thunk,byte[] flag,Http http);

    public ChunkParser(){
        this(null);
    }

    public ChunkParser(ChunkFlagResolver resolver){
        mChunkFlagResolver=resolver;
    }

    protected void onChunkChecked(byte[] chunk){
        //Do nothing
    }

    protected byte[] onResolveChunkFlag(Answer answer,Http http){
        return null;
    }

    @Override
    public R onParse(Http http, Answer answer) {
        if (null==answer){
            Debug.E("Fail parse while http answer null.");
            return onChunkParseFinish(Code.CODE_ARGS_INVALID,null,null,http);
        }
        final Headers headers=answer.getHeaders();
        String encoding=null!=headers?headers.getTransferEncoding():null;
        if (null==encoding||!encoding.equals(Headers.CHUNKED)){
            Debug.E("Fail parse while http transfer encoding NOT chunked."+encoding);
            return onChunkParseFinish(Code.CODE_FAIL,null,null,http);
        }
        ChunkFlagResolver resolver=mChunkFlagResolver;
        ChunkFinder chunkFinder=null; byte[] thunkFlag=null;
        try {
            thunkFlag=null!=resolver?resolver.onResolveChunkFlag(answer):null;
            if (null==thunkFlag||thunkFlag.length<=0){
                thunkFlag=onResolveChunkFlag(answer,http);
                Debug.D("Use http response head chunk flag to read chunk."+thunkFlag);
            }
            chunkFinder=new ChunkFinder(thunkFlag);
            AnswerBody answerBody=null!=answer?answer.getAnswerBody():null;
            java.io.InputStream inputStream=null!=answerBody?answerBody.getInputStream():null;
            final byte[] buffer=new byte[1024];
            int length=0;byte[] chunk=null;
            while ((length=inputStream.read(buffer))>=0){
                if (length>0){
                    chunkFinder.write(buffer,0,length);
                    Debug.D("SFDSAFDSADFA length="+length+" "+new String(thunkFlag));
                    if (null==(chunk=chunkFinder.checkChunk())){
                        continue;//Circle to again to until read one chunk
                    }
                    onChunkChecked(chunk);
                }
            }
            return onChunkParseFinish(Code.CODE_SUCCEED,chunkFinder.toByteArray(),thunkFlag,http);
        }catch (Exception e){
            Debug.E("Exception parse http thunk.e="+e,e);
            e.printStackTrace();
            if (e instanceof EOFException){
                return onChunkParseFinish(Code.CODE_CANCEL,null!=chunkFinder? chunkFinder.toByteArray():null,thunkFlag,http);
            }
        }
        return onChunkParseFinish(Code.CODE_FINISH,null!=chunkFinder?chunkFinder.toByteArray():null,thunkFlag,http);
    }

    protected static class ChunkFinder extends ByteArrayOutputStream{
        protected final byte[] mFlag;
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
