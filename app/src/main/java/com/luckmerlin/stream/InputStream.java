package com.luckmerlin.stream;

import java.io.IOException;

public abstract class InputStream extends AbstractStream {
    private long mReadLength;

    public InputStream(long openLength,Convertor convertor){
        super(openLength,convertor);
    }

    @Override
    public final long getReadOrWriteLength() {
        return mReadLength;
    }

    public abstract long length();

    public final int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public final int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;
        mReadLength++;
        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                mReadLength++;
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

    protected abstract int onRead()throws IOException;

    private final int read() throws IOException{
        int data=onRead();
        Convertor convertor=mConvertor;
        return null!=convertor?convertor.onConvert(data,this):data;
    }
}
