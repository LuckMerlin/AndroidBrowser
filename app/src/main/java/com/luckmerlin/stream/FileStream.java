package com.luckmerlin.stream;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStream extends AbstractStream {
    private final File mFile;
    private Closeable mOpenStream;

    public FileStream(File file){
        mFile=file;
    }

    @Override
    public InputStream openInputStream(long skip)throws Exception {
        Closeable openStream=mOpenStream;
        if (null!=openStream){
            if (!(openStream instanceof InputStream)){
                return null;
            }
            return (InputStream)openStream;
        }
        File file=mFile;
        if (null==file){
            return null;
        }else if (!file.exists()){
            return null;
        }else if (!file.isFile()){
            return null;
        }
        final FileInputStream fileInputStream=new FileInputStream(file);
        if (skip>0){
            fileInputStream.skip(skip);
        }
        InputStream inputStream= new InputStream() {
            @Override
            public long length() {
                return file.length();
            }

            @Override
            public int read() throws IOException {
                return fileInputStream.read();
            }

            @Override
            public void close() throws IOException {
                closeStream(fileInputStream);
                Closeable openStream=mOpenStream;
                if (null!=openStream&&openStream==this){
                    mOpenStream=null;
                }
            }
        };
        mOpenStream=inputStream;
        return inputStream;
    }

    @Override
    public OutputStream openOutputStream() throws Exception{
        Closeable openStream=mOpenStream;
        if (null!=openStream){
            if (!(openStream instanceof OutputStream)){
                return null;
            }
            return (OutputStream)openStream;
        }
        File file=mFile;
        if (null==file){
            return null;
        }else if (!file.exists()){
            File parent=file.getParentFile();
            if (null==parent){
                return null;
            }else if (!parent.exists()){
                parent.mkdirs();
            }
            file.createNewFile();
            if (!file.exists()){
                return null;
            }
        }
        final FileOutputStream fileOutputStream=new FileOutputStream(file);
        return new OutputStream(file.length()) {
            @Override
            public void write(int b) throws IOException {
                fileOutputStream.write(b);
            }

            @Override
            public void close() throws IOException {
                closeStream(fileOutputStream);
                Closeable openStream=mOpenStream;
                if (null!=openStream&&openStream==this){
                    mOpenStream=null;
                }
            }
        };
    }

    @Override
    public void close() {
        closeStream(mOpenStream);
    }
}
