package com.luckmerlin.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public abstract class AnswerBody {
    private boolean mAutoClose=true;//Default true to auto close
    public abstract long getContentLength();
    public abstract String getContentType();

    public abstract InputStream getInputStream();

    public boolean autoClose(boolean enable){
        if (mAutoClose!=enable){
            mAutoClose=enable;
            return true;
        }
        return false;
    }

    public boolean isAutoClose() {
        return mAutoClose;
    }

    public final byte[] getBytesSafe(int max, byte[] def){
        try {
            return getBytes(max);
        } catch (Exception e) {
            return def;
        }
    }

    public final byte[] getBytes(int max) throws Exception{
        InputStream inputStream=getInputStream();
        if (null==inputStream){
            return null;
        }
        int available=inputStream.available();
        if (available>=0){
            final int def=1024*1024*10;
            byte[] bytes=new byte[Math.min(max<=0?def:max,Math.min(available,def))];
            int read=inputStream.read(bytes);
            return read>0?Arrays.copyOf(bytes,read):null;
        }
        return null;
    }

    public final String getTextSafe(String charsetName,String def){
        try {
            return getText(charsetName);
        } catch (IOException e) {
            return def;
        }
    }

    public final String getText(String charsetName) throws IOException {
        InputStream inputStream=getInputStream();
        BufferedInputStream bufferedInputStream=null!=inputStream?new BufferedInputStream(inputStream):null;
        if (null==bufferedInputStream){
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        int read=-1;byte[] buffer=new byte[1024];
        while ((read=bufferedInputStream.read(buffer))>=0){
            byteArrayOutputStream.write(buffer,0,read);
        }
        return byteArrayOutputStream.toString(null!=charsetName&&charsetName.length()>0?charsetName:"utf-8");
    }
}
