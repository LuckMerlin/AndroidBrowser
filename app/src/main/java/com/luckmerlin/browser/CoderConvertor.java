package com.luckmerlin.browser;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.Convertor;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.stream.Stream;

public class CoderConvertor implements Convertor {

    @Override
    public int onConvert(int data, Stream stream) {
        if (null!=stream){
            if (stream instanceof InputStream){
//                return (~data);
//                return data;
            }else if (stream instanceof OutputStream){
                return (~data);
            }
        }
        if (stream.getTotal()>2170)
            Debug.D("EEEEEEEE "+stream.getTotal()+" "+stream.
                    getOpenLength()+" "+" "+(stream instanceof InputStream));
        return data;
    }
}
