package com.luckmerlin.stream;

import com.luckmerlin.debug.Debug;

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
    public InputStream openInputStream(long skip,Convertor convertor)throws Exception {
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
        InputStream inputStream= new InputStream(skip,convertor) {
            @Override
            public long length() {
                return file.length();
            }

            @Override
            protected int onRead() throws IOException {
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
    public OutputStream openOutputStream(Convertor convertor) throws Exception{
        Closeable openStream=mOpenStream;
        if (null!=openStream){
            if (!(openStream instanceof OutputStream)){
                Debug.D("Fail open file output stream while opened not match stream.");
                return null;
            }
            Debug.D("Use opened output stream.");
            return (OutputStream)openStream;
        }
        File file=mFile;
        if (null==file){
            Debug.D("Fail open output stream while file invalid.");
            return null;
        }else if (!file.exists()){
            File parent=file.getParentFile();
            if (null!=parent&&!parent.exists()){
                Debug.D("Create folder before  open output stream."+parent);
                parent.mkdirs();
            }
            Debug.D("Create file before open output stream."+file);
            file.createNewFile();
            if (!file.exists()){
                Debug.D("Fail open output stream while file create fail.");
                return null;
            }
        }
        final FileOutputStream fileOutputStream=new FileOutputStream(file);
        OutputStream outputStream= new OutputStream(file.length(),convertor) {
            @Override
            protected void onWrite(int b) throws IOException {
                fileOutputStream.write(b);
            }

            @Override
            public void close() throws IOException {
                closeStream(fileOutputStream);
                Closeable openStream=mOpenStream;
                Debug.D("Close file streams.");
                if (null!=openStream&&openStream==this){
                    Debug.D("All file stream closed.");
                    mOpenStream=null;
                }
            }
        };
        mOpenStream=outputStream;
        return outputStream;
    }

    @Override
    public void close() {
        closeStream(mOpenStream);
    }
}
