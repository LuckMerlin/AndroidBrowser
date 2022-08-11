package com.luckmerlin.browser;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.Convertor;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.stream.Stream;

import java.io.UnsupportedEncodingException;

public class CoderConvertor implements Convertor {
    private byte[] test;
    {
        try {
            test = "我们的".getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onConvert(int data, Stream stream) {
        if (null!=stream){
            long total=stream.getTotal();
            int sect=(int)(total%test.length);

            if (stream instanceof InputStream){
//                test.length;
            }else if (stream instanceof OutputStream){

            }
        }
        if (stream.getTotal()>2170)
            Debug.D("EEEEEEEE "+stream.getTotal()+" "+stream.
                    getOpenLength()+" "+" "+(stream instanceof InputStream));
        return data;
    }
}
