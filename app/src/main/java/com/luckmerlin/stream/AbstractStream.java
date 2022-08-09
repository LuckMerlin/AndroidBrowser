package com.luckmerlin.stream;

import com.luckmerlin.debug.Debug;

import java.io.Closeable;
import java.io.IOException;

public abstract class AbstractStream implements Stream{

//    public final boolean writeStream(Stream stream){
//        return writeStream(stream,null);
//    }
//
//    public final boolean writeStream(Stream stream,OnProgressChange progressChange){
//        if (null==stream){
//            Debug.E("Fail write stream while stream is invalid.");
//            return false;
//        }
//        OutputStream outputStream=null;InputStream inputStream=null;
//        try {
//            outputStream=openOutputStream();
//            if (null==outputStream){
//                Debug.E("Fail write stream while output stream open failed.");
//                return false;
//            }
//            Debug.D("Opened output stream for write stream.");
//            final long currentLength=outputStream.getTotal();
//            inputStream=stream.openInputStream(currentLength<=0?0:currentLength);
//            if (null==inputStream){
//                Debug.E("Fail write stream while input stream open failed.");
//                return false;
//            }
//            final long inputTotal=inputStream.length();
//            Debug.D("Opened input stream for write stream.currentLength="+currentLength+" inputTotal="+inputTotal);
//            notifyProgressChange(currentLength,inputTotal,progressChange);
//            final int bufferLength=1024;
//            int read=0;
//            byte[] buffer=new byte[bufferLength];
//            while ((read=inputStream.read(buffer,0,bufferLength))>=0){
//                if (read<=0){
//                    continue;
//                }
//                outputStream.write(buffer,0,read);
//                if (!notifyProgressChange(outputStream.getTotal(),inputTotal,progressChange)){
//                    break;
//                }
//            }
//            return true;
//        } catch (Exception e) {
//            Debug.E("Exception write stream."+e);
//            e.printStackTrace();
//        }finally {
//            closeStream(outputStream,inputStream);
//        }
//        return false;
//    }

    public final boolean notifyProgressChange(long current,long total,long speed,OnProgressChange progressChange){
        return null==progressChange||progressChange.onProgressChange(current,total,speed);
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
