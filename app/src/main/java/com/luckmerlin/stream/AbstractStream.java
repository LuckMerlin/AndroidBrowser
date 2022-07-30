package com.luckmerlin.stream;

import java.io.Closeable;
import java.io.IOException;

public abstract class AbstractStream implements Stream{

    public final boolean writeStream(Stream stream){
        return writeStream(stream,null);
    }

    public final boolean writeStream(Stream stream,OnProgressChange progressChange){
        if (null==stream){
            return false;
        }
        OutputStream outputStream=null;InputStream inputStream=null;
        try {
            outputStream=openOutputStream();
            if (null==outputStream){
                return false;
            }
            final long currentLength=outputStream.length();
            inputStream=stream.openInputStream(currentLength<=0?0:currentLength);
            if (null==inputStream){
                return false;
            }
            final long inputTotal=inputStream.length();
            notifyProgressChange(currentLength,inputTotal,progressChange);
            final int bufferLength=1024;
            int read=0;
            byte[] buffer=new byte[bufferLength];
            while ((read=inputStream.read(buffer,0,bufferLength))>=0){
                if (read<=0){
                    continue;
                }
                outputStream.write(buffer,0,read);
                if (!notifyProgressChange(outputStream.length(),inputTotal,progressChange)){
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            closeStream(outputStream,inputStream);
        }
        return false;
    }

    public final boolean notifyProgressChange(long current,long total,OnProgressChange progressChange){
        return null==progressChange||progressChange.onProgressChange(current,total);
    }

    public final void closeStream(Closeable ... closeables){
        if (null!=closeables){
            for (Closeable child:closeables) {
                if (null!=child){
                    try {
                        child.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
