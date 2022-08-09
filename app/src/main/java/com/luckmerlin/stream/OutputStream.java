package com.luckmerlin.stream;

import java.io.Closeable;
import java.io.IOException;

public abstract class OutputStream implements Closeable {
    private long mWritten=0;
    private long mOpenLength;
    private final Convertor mConvertor;

    public OutputStream(long openLength,Convertor convertor){
        mOpenLength=openLength;
        mConvertor=convertor;
    }

    protected abstract void onWrite(int b)throws IOException;

    public final void write(int b) throws IOException{
        Convertor convertor=mConvertor;
        onWrite(null!=convertor?convertor.onConvert(b):b);
        mWritten++;
    }

    public final void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public final void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

    public final long getTotal(){
        long writeLength= getWrittenLength();
        long openLength= getOpenLength();
        return (writeLength>=0?writeLength:0)+(openLength>=0?openLength:0);
    }

    public final long getOpenLength() {
        return mOpenLength;
    }

    public final long getWrittenLength(){
        return mWritten;
    }

}
