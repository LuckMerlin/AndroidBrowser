package com.luckmerlin.stream;

import java.io.IOException;

public class StreamCopier {

    public final boolean copy(InputStream inputStream,OutputStream outputStream,byte[] buffer,
                        OnProgressChange progressChange) throws IOException {
        if (null==inputStream||null==outputStream){
            return false;
        }
        buffer=null!=buffer&&buffer.length>0?buffer:new byte[1024];
        int bufferLength=buffer.length;
        int read=-1;long total=inputStream.getTotal();
        while ((read=inputStream.read(buffer,0,bufferLength))>=0){
            if (read<=0){
                continue;
            }
            outputStream.write(buffer,0,read);
            if (null==progressChange||progressChange.onProgressChange(outputStream.getTotal(),total,0)){
                continue;
            }
            return false;
        }
        return true;
    }
}
