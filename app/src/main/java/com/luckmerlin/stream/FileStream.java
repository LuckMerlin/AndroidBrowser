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
                Debug.W("Fail open file input stream while already open but not as InputStream. "+openStream);
                return null;
            }
            return (InputStream)openStream;
        }
        File file=mFile;
        if (null==file){
            Debug.W("Fail open file input stream while file invalid.");
            return null;
        }else if (!file.exists()){
            Debug.W("Fail open file input stream while file not exist.");
            return null;
        }else if (!file.isFile()){
            Debug.W("Fail open file input stream while file is not file.");
            return null;
        }
        Debug.W("Opening file input stream.");
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
        }.setTitle(file.getName());
        Debug.W("Opened file input stream.");
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
        Debug.W("Opening file output stream."+file);
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
        Debug.W("Opened file output stream.");
        return outputStream;
    }

    @Override
    public void close() {
        closeStream(mOpenStream);
    }
}
