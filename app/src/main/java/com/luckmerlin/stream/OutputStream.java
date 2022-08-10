package com.luckmerlin.stream;

import java.io.IOException;

public abstract class OutputStream extends AbstractStream {
    private long mWritten=0;

    public OutputStream(long openLength,Convertor convertor){
        super(openLength,convertor);
    }

    @Override
    public final long getReadOrWriteLength() {
        return mWritten;
    }

    protected abstract void onWrite(int b)throws IOException;

    public final void write(int b) throws IOException{
        Convertor convertor=mConvertor;
        mWritten++;
        onWrite(null!=convertor?convertor.onConvert(b,this):b);
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

}
