//package com.luckmerlin.browser.http;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.luckmerlin.debug.Debug;
//import com.luckmerlin.http.Answer;
//import com.luckmerlin.http.Headers;
//import com.luckmerlin.http.Http;
//import com.luckmerlin.http.OnOutputStreamOpen;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import okhttp3.Authenticator;
//import okhttp3.Requested;
//import okhttp3.Interceptor;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//import okio.BufferedSink;
//
//public class OKHttp extends Http {
//    private final OkHttpClient.Builder mBuilder;
//
//    public OKHttp(){
//        mBuilder=new OkHttpClient.Builder();
//        callTimeout(10,TimeUnit.SECONDS);
//        connectTimeout(10,TimeUnit.SECONDS);
//    }
//
//    @Override
//    protected Answer onCall(final String method, String url, com.luckmerlin.http.Request request) {
//        if (null==request){
//            return null;
//        }
////        OnOutputStreamOpen onOutputStreamOpen=request.getOnOutputStreamOpen();
////        final InputStream[] inputStreams=new InputStream[1];
////        final byte[] buffer=new byte[1024];
//        final String finalMethod=(null!=method?method: com.luckmerlin.http.Request.METHOD_GET).toUpperCase();
//        final RequestBody requestBody=RequestBody.create(new byte[0]);
//        Headers headers=null!=request?request.headers():null;
//        okhttp3.Headers okHttpHeaders=okhttp3.Headers.of(null!=headers?headers:new HashMap<>(0));
//        Debug.D("[OkHttp] "+finalMethod+" "+url);
//        Requested call=newCall(new Request.Builder().method(finalMethod,requestBody).
//                headers(okHttpHeaders).url(url).build());
//        try {
//            Response response=call.execute();
//            return null!=response?new HttpResponse(response):null;
//        } catch (Exception e) {
//            Debug.D("Exception  e="+e);
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private Requested newCall(Request request){
//        OkHttpClient.Builder builder=mBuilder;
//        OkHttpClient client=null!=builder?builder.build():null;
//        return null!=client?client.newCall(request):null;
//    }
//
//    private boolean connectTimeout(long time, TimeUnit unit){
//        OkHttpClient.Builder builder=mBuilder;
//        if (null!=unit&&null!=builder){
//            builder.connectTimeout(time,unit);
//            return true;
//        }
//        return false;
//    }
//
//    private boolean callTimeout(long time, TimeUnit unit){
//        OkHttpClient.Builder builder=mBuilder;
//        if (null!=unit&&null!=builder){
//            builder.callTimeout(time,unit);
//            return true;
//        }
//        return false;
//    }
//
//    private boolean authenticator(Authenticator authenticator){
//        OkHttpClient.Builder builder=mBuilder;
//        if (null!=authenticator&&null!=builder){
//            builder.authenticator(authenticator);
//            return true;
//        }
//        return false;
//    }
//
//    private boolean addNetworkInterceptor(Interceptor interceptor){
//        OkHttpClient.Builder builder=mBuilder;
//        if (null!=interceptor&&null!=builder){
//            builder.addNetworkInterceptor(interceptor);
//            return true;
//        }
//        return false;
//    }
//
//    private boolean addInterceptor(Interceptor interceptor){
//        OkHttpClient.Builder builder=mBuilder;
//        if (null!=interceptor&&null!=builder){
//            builder.addInterceptor(interceptor);
//            return true;
//        }
//        return false;
//    }
//}
