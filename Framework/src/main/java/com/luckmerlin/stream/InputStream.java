package com.luckmerlin.stream;

import java.io.IOException;

public abstract class InputStream extends AbstractStream {
    private long mReadLength;

    public InputStream(long openLength){
        super(openLength);
    }

    @Override
    public final long getReadOrWriteLength() {
        return mReadLength;
    }

    @Deprecated
    public abstract long length();

    public final long getTotalLength(){
        return length();
    }

    public final int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public final int read(byte b[], int off, int len) throws IOException {
        int read=onRead(b,off,len);
        mReadLength+=(read>0?read:0);
        return read;
    }

    public abstract int onRead(byte b[], int off, int len) throws IOException;
}
