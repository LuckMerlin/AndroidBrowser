package com.luckmerlin.http;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.object.ObjectCreator;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Http {
    private String mBaseUrl;

    public final Http setBaseUrl(String url){
       mBaseUrl=url;
       return this;
    }

    protected abstract Connection onConnect(String method, String url, Request request);

    public final Connection connect(Request call){
        String baseUrl=null!=call?call.baseUrl():null;
        baseUrl=null!=baseUrl?baseUrl:mBaseUrl;
        String url=null!=call?call.url():null;
        String method=null!=call?call.method():null;
        method=null!=method?method: Request.METHOD_GET;
        return onConnect(method,(null!=baseUrl?baseUrl:"")+(null!=url?url:""),call);
    }

    public final <T> T call(Request request,OnHttpParse<T> parser){
        return call(request,null,parser);
    }

    public final <T> T call(Request request,InputStream input,OnHttpParse<T> parser){
        Connection connection=connect(request);
        if (null==connection){
            Debug.E("Fail call http connection while connect fail.");
            return null;
        }
        return call(connection,input,parser);
    }

    public final <T> T call(Connection connection, InputStream input,OnHttpParse<T> parser){
        Requested requested=null!=connection?connection.getRequested():null;
        if (null==requested){
            Debug.E("Fail call http connection while requested invalid.");
            return null;
        }
        try {
            //Try write data into stream
            if (null!=input){
                OutputStream outputStream=requested.getOutputStream();
                if (null==outputStream){
                    Debug.E("Fail call http connection while output stream invalid.");
                    return null;
                }
                int read=0;byte[] buffer=new byte[1024];
                while ((read=input.read(buffer))>=0){
                    if (read>0){
                        outputStream.write(buffer,0,read);
                    }
                }
                Debug.D("Written data into http connection.");
            }
            //Try parse answer
            return null!=parser?parser.onParse(Http.this,requested.getAnswer()):null;
        } catch (IOException e) {
            e.printStackTrace();
            Debug.E("Exception call http connection.e="+e,e);
            return null;
        }finally {
           closes(connection);
        }
    }

    protected final void closes(Closeable... closeables) {
        if (null!=closeables){
            for (Closeable child:closeables) {
                if (null!=child){
                    try {
                        child.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
