package com.luckmerlin.browser.http;

import androidx.annotation.NonNull;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.Connection;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Requested;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;
import okio.Sink;
import okio.Source;
import okio.Timeout;

public class OKHttp extends Http {
    private final OkHttpClient.Builder mBuilder;

    public OKHttp(){
        mBuilder=new OkHttpClient.Builder();
    }

    public Connection test(String method, String url, Request request){
        return onConnect(method,url,request);
    }

    @Override
    protected Connection onConnect(String method, String url, Request request) {
        try {
            RequestBody requestBody=method.toLowerCase().equals("post")?RequestBody.create(new byte[0]):null;
            Headers headers=null!=request?request.headers():null;
            okhttp3.Headers okHttpHeaders=okhttp3.Headers.of(null!=headers?headers:new HashMap<>(0));
            okhttp3.Request request1=new okhttp3.Request.Builder().method(method,requestBody).
                    headers(okHttpHeaders).url(url).build();

            OkHttpClient.Builder builder=mBuilder;
//            builder.
            OkHttpClient client=null!=builder?builder.build():null;
            Response response=client.newCall(request1).execute();
//            client.proxy().
//            InputStream inputStream=response.body().byteStream();
//            int read=0;byte[] buffer=new byte[1024*102410];
//            long time=System.currentTimeMillis();
//            while ((read=inputStream.read(buffer))>=0){
//                Debug.D("EEEEE "+read+" "+(System.currentTimeMillis()-time));
//                time=System.currentTimeMillis();
//            }

//            BufferedSource source=response.body().source();
//            Buffer buffer=new Buffer();
//            source.readFully(buffer,1024*900);
//            int read=0;byte[] buffer1=new byte[1024*900];
//            long ddd;
//            InputStream inputStream=buffer.inputStream();
//            while ((ddd=inputStream.read(buffer1))>0){
//                Debug.D("EEEE "+ddd);
//            }
//            source.readFully();
//            if (null!=url&&url.contains("file/inputStream")){
//                int read=0;byte[] buffer=new byte[1024*1024];
//                long time=System.currentTimeMillis();
//                while ((read=inputStream.read(buffer,0,buffer.length))>=0){
//                    Debug.D("EEEEE "+read+" "+(System.currentTimeMillis()-time));
//                    time=System.currentTimeMillis();
//                }
//            }

//            BufferedSource bufferedSource=response.body().source();
//            if (null!=url&&url.contains("file/inputStream")){
//                Buffer buffer2=new Buffer();
//                long time=System.currentTimeMillis();
//                long read=0;
//                while ((read=bufferedSource.read(buffer2,1024*1024))>=0){
//                    Debug.D("EEEEE "+read+" "+(System.currentTimeMillis()-time));
//                    time=System.currentTimeMillis();
//                }
//            }
        } catch (IOException e) {
            Debug.D("EEEE "+e);
            e.printStackTrace();
        }
        return null;
    }
//
//    private Requested newCall(okhttp3.Request request){
//        OkHttpClient.Builder builder=mBuilder;
//        OkHttpClient client=null!=builder?builder.build():null;
//        return null!=client?client.newCall(request):null;
//    }
}
