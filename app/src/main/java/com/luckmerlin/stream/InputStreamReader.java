package com.luckmerlin.stream;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamReader {
    protected InputStream mInputStream;
    private long mHasReadLength;
    private long mContentLength=-1;
    private OnEndBytesRead mOnEndBytesRead;

    public interface OnEndBytesRead{
        void onEndBytesRead(byte[] buffer,int offset,int size);
    }

    public InputStreamReader(InputStream inputStream,long contentLength){
        mHasReadLength=0;
        setInputStream(inputStream,contentLength);
    }

    public final InputStreamReader setOnEndBytesRead(OnEndBytesRead onEndBytesRead) {
        this.mOnEndBytesRead = onEndBytesRead;
        return this;
    }

    public final InputStreamReader setInputStream(InputStream inputStream,long contentLength){
        mContentLength=contentLength;
        mInputStream=null!=inputStream?inputStream:null;
        return this;
    }

    public final long getContentLength(){
        return mContentLength;
    }

//    public final int readAll(byte[] buffer,final long length,OnChangeUpdate<Integer> bytesRead, OnFinish<byte[]> onFinish)throws IOException{
//        InputStream inputStream=mInputStream;
//        if (null!=inputStream&&null!=buffer&&buffer.length>0&&null!=bytesRead){
//            int read=0;long totalRead=0;long lastRead=0;
//            ByteArrayOutputStream endBytesStream=null;
//            while ((read=inputStream.read(buffer))>=0){
//                lastRead=totalRead;
//                totalRead+=read;
//                if (length>0&&totalRead>length) {
//                    if (null != endBytesStream) {
//                        endBytesStream.write(buffer, 0, read);
//                    } else if (lastRead <= length) {
//                        endBytesStream = new ByteArrayOutputStream();
//                        endBytesStream.write(buffer, read = (int) (length - lastRead), (int) (totalRead - length));
//                        bytesRead.onChangeUpdated(read);
//                    }
//                    continue;
//                }
//                if (bytesRead.onChangeUpdated(read)){
//                    break;
//                }
//            }
//            if (null!=onFinish){
//                onFinish.onFinish(null!=endBytesStream?endBytesStream.toByteArray():null);
//            }
//        }
//        return -1;
//    }

    public int read(byte buffer[]) throws IOException {
        return read(buffer, 0, null!=buffer?buffer.length:0);
    }

    public final int read(byte[] buffer,int offset,int size) throws IOException {
        InputStream inputStream=mInputStream;
        int readSize= null!=inputStream?inputStream.read(buffer,offset,size):0;
        long lastHadReadLength=mHasReadLength;
        mHasReadLength+=(readSize>=0?readSize:0);
        long contentLength=getContentLength();
        if (contentLength<0||mHasReadLength<=contentLength){
            return readSize;
        }else if (lastHadReadLength<=contentLength){
            int fullOffset=(int)(contentLength - lastHadReadLength);
            notifyEndBytesRead(buffer,fullOffset,(int)(mHasReadLength-contentLength));
            return fullOffset;
        }
        if (readSize<0){
            return readSize;
        }
        notifyEndBytesRead(buffer,0,readSize);
        return 0;
    }

    private void notifyEndBytesRead(byte[] buffer,int offset,int size){
        OnEndBytesRead onEndBytesRead=mOnEndBytesRead;
        if (null!=onEndBytesRead){
            onEndBytesRead.onEndBytesRead(buffer,offset,size);
        }
    }

}
