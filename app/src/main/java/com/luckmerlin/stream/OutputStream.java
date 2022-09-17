package com.luckmerlin.stream;

import java.io.IOException;

public abstract class OutputStream extends AbstractStream {
    private long mWritten=0;

    public OutputStream(long openLength){
        super(openLength);
    }

    @Override
    public final long getReadOrWriteLength() {
        return mWritten;
    }

    public final void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    protected abstract void onWrite(byte b[], int off, int len) throws IOException;

    public final void write(byte b[], int off, int len) throws IOException {
        onWrite(b,off,len);
        mWritten+=(len>0?len:0);
    }

}
