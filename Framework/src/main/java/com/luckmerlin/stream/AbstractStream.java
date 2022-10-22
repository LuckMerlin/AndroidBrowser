package com.luckmerlin.stream;

import java.io.Closeable;

public abstract class AbstractStream implements Closeable,Stream{
    private String mTitle;
    private long mOpenLength=0;

    public AbstractStream(long openLength){
        mOpenLength=openLength;
    }

    public abstract long getReadOrWriteLength();

    @Override
    public final long getOpenLength() {
        return mOpenLength;
    }

    public final long getTotal(){
        long writeLength= getReadOrWriteLength();
        long openLength= getOpenLength();
        return (writeLength>=0?writeLength:0)+(openLength>=0?openLength:0);
    }

    public final AbstractStream setTitle(String title) {
        mTitle = title;
        return this;
    }

    @Override
    public final String getTitle() {
        return mTitle;
    }
}
