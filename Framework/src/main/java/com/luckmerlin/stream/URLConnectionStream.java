package com.luckmerlin.stream;

import android.os.Build;
import com.luckmerlin.debug.Debug;
import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public abstract class URLConnectionStream extends NetStream{
    private HttpStreams mOpenedHttpStream;

    protected abstract HttpStreams onConnectionHttp(Convertor convertor)throws Exception;

    public static URLConnection createConnection(String url) throws IOException {
        return null!=url&&url.length()>0?createConnection(new URL(url)):null;
    }

    public static URLConnection createConnection(URL url) throws IOException {
        return null!=url?url.openConnection():null;
    }

    @Override
    public InputStream openInputStream(long skip,Convertor convertor) throws Exception {
        HttpStreams httpStreams=mOpenedHttpStream;
        if (null!=httpStreams){
            Debug.D("Use cached http input stream.");
            return httpStreams.mInputStream;
        }
        httpStreams=mOpenedHttpStream=onConnectionHttp(convertor);
        if (null==httpStreams){
            Debug.D("Fail open http input stream while connection create fail.");
            return null;
        }
        return httpStreams.mInputStream;
    }

    @Override
    public OutputStream openOutputStream(Convertor convertor) throws Exception {
        HttpStreams httpStreams=mOpenedHttpStream;
        if (null!=httpStreams){
            Debug.D("Use cached http output stream.");
            return httpStreams.mOutputStream;
        }
        httpStreams=mOpenedHttpStream=onConnectionHttp(convertor);
        if (null==httpStreams){
            Debug.D("Fail open http output stream while connection create fail.");
            return null;
        }
        return httpStreams.mOutputStream;
    }

    @Override
    public final void close() throws IOException {
        closeStream(mOpenedHttpStream);
        mOpenedHttpStream=null;
    }

    public class HttpStreams implements Closeable{
        private URLConnection mURLConnection;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private final long mOutputOpenLength;
        private Long mInputLength;
        private Convertor mConvertor;

        public HttpStreams(URLConnection connection,long outputOpenLength,Convertor convertor){
            mURLConnection=connection;
            mOutputOpenLength=outputOpenLength;
            mConvertor=convertor;
        }

        protected HttpStreams connect() throws IOException {
            URLConnection connection=mURLConnection;
            if (null==connection){
                return this;
            }
            connection.connect();
            final java.io.OutputStream outputStream=connection.getDoOutput()?connection.getOutputStream():null;
            if (null!=outputStream){
                mOutputStream=new OutputStream(mOutputOpenLength) {
                    @Override
                    protected void onWrite(byte[] b, int off, int len) throws IOException {
                        outputStream.write(b,off,len);
                    }

                    @Override
                    public void close() throws IOException {
                        outputStream.close();
                    }
                };
            }
            final java.io.InputStream inputStream=connection.getDoInput()?connection.getInputStream():null;
            if (null!=inputStream){
                mInputStream=new InputStream(0) {
                    @Override
                    public long length() {
                        Long inputLength=mInputLength;
                        return null!=inputLength?inputLength:(mInputLength=Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?
                                connection.getContentLengthLong():connection.getContentLength());
                    }

                    @Override
                    public int onRead(byte[] b, int off, int len) throws IOException {
                        return inputStream.read(b,off,len);
                    }

                    @Override
                    public void close() throws IOException {
                        inputStream.close();
                    }
                };
            }
            return this;
        }

        protected void disconnect()throws IOException {
            this.close();
        }

        @Override
        public final void close() throws IOException {
            closeConnection(mURLConnection);
            mURLConnection=null;
            URLConnectionStream.super.closeStream(mInputStream,mOutputStream);
        }

        private void closeConnection(URLConnection urlConnection){
            if (null!=urlConnection&&urlConnection instanceof HttpURLConnection){
                ((HttpURLConnection)urlConnection).disconnect();
            }
        }
    }

}
