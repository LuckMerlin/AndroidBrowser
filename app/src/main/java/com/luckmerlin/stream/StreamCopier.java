package com.luckmerlin.stream;

import com.luckmerlin.debug.Debug;

import java.io.IOException;

public class StreamCopier {

    public final boolean copy(InputStream inputStream,OutputStream outputStream,byte[] buffer,
                        OnProgressChange progressChange) throws IOException {
        if (null==inputStream||null==outputStream){
            Debug.D("Fail copy stream while inputStream or outputStream invalid."+outputStream);
            return false;
        }
        buffer=null!=buffer&&buffer.length>0?buffer:new byte[1024];
        int bufferLength=buffer.length;
        int read=-1;long total=inputStream.length();
        Debug.D("Copy stream from into output."+outputStream.getTotal()+"/"+total);
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
        long outTotal=outputStream.getTotal();
        if (total!=outTotal){
            Debug.D("Fail copy stream from while result length not matched."+outTotal+"/"+total);
            return false;
        }
        Debug.D("Succeed copy stream from."+outputStream.getTotal()+"/"+total);
        return true;
    }
}
