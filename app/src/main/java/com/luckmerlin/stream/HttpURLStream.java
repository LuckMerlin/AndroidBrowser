package com.luckmerlin.stream;

import android.os.Build;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class HttpURLStream extends NetStream{
    private final String mStreamUrl;
    private URLConnection mOpenURLConnection;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private java.io.InputStream mHttpInputStream;
    private java.io.OutputStream mHttpOutputStream;
    private long mContentLength=0;

    public HttpURLStream(String streamUrl){
        mStreamUrl=streamUrl;
    }

    @Override
    public InputStream openInputStream(long skip,Convertor convertor) throws Exception {
        InputStream inputStream=mInputStream;
        if (null!=inputStream){
            return inputStream;
        }
        java.io.InputStream httpInputStream=mHttpInputStream;
        if (null==httpInputStream){
            openConnection(mStreamUrl);
        }
        if (null==(httpInputStream=mHttpInputStream)){
            return null;
        }
        final java.io.InputStream finalHttpInputStream=httpInputStream;
        return mInputStream=new InputStream(0,convertor) {
            @Override
            public long length() {
                return mContentLength;
            }

            @Override
            protected int onRead() throws IOException {
                return finalHttpInputStream.read();
            }

            @Override
            public void close() throws IOException {
                if (null!=finalHttpInputStream){

                }
            }
        };
    }

    @Override
    public OutputStream openOutputStream(Convertor convertor) throws Exception {
        return null;
    }


    @Override
    public final void close() throws IOException {
        closeConnection(mOpenURLConnection);
        mOpenURLConnection=null;
        closeStream(mInputStream,mOutputStream);
    }

    private boolean openConnection(String streamUrl) throws Exception{
        mContentLength=0;
        URLConnection urlConnection= null!=streamUrl&&streamUrl.length()>0?new URL(streamUrl).openConnection():null;
        if (null!=urlConnection){
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            java.io.InputStream inputStream=urlConnection.getInputStream();
            java.io.OutputStream outputStream=urlConnection.getOutputStream();
            if (null==inputStream||null==outputStream){
                closeConnection(urlConnection);
                closeStream(inputStream,outputStream);
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mContentLength=urlConnection.getContentLengthLong();
            }else{
                mContentLength=urlConnection.getContentLength();
            }
            mHttpInputStream=inputStream;
            mHttpOutputStream=outputStream;
            return true;
        }
        return false;
    }

    private void closeConnection(URLConnection urlConnection){
        if (null!=urlConnection&&urlConnection instanceof HttpURLConnection){
            ((HttpURLConnection)urlConnection).disconnect();
        }
    }

}
