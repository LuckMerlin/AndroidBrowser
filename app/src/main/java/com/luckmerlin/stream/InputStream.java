package com.luckmerlin.stream;

import java.io.Closeable;
import java.io.IOException;

public abstract class InputStream implements Closeable {
    private long mReadLength=0;
    private long mOpenLength;
    private final Convertor mConvertor;
    private String mTitle;

    public InputStream(long openLength,Convertor convertor){
        mOpenLength=openLength;
        mConvertor=convertor;
    }

    public final long getOpenLength() {
        return mOpenLength;
    }

    public final long getReadLength() {
        return mReadLength;
    }

    public final InputStream setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public final String getTitle(){
        return mTitle;
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

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

    protected abstract int onRead()throws IOException;

    public final int read() throws IOException{
        int data=onRead();
        mReadLength++;
        Convertor convertor=mConvertor;
        return null!=convertor?convertor.onConvert(true,data):data;
    }

    public final long getTotal(){
        long readLength= getTotal();
        long openLength= getOpenLength();
        return (readLength>=0?readLength:0)+(openLength>=0?openLength:0);
    }
}
