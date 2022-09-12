package com.luckmerlin.browser.http;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.AnswerBody;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Requested;
import com.luckmerlin.http.Connection;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.OnOutputStreamOpen;
import com.luckmerlin.http.Answer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class JavaHttp extends Http {

    @Override
    protected Connection onConnect(String method, String url, Request request) {
        if (null==request){
            Debug.E("Fail connect java http while request invalid.");
            return null;
        }else if (null==method){
            Debug.E("Fail connect java http while method invalid.");
            return null;
        }else if (null==url||url.length()<=0){
            Debug.E("Fail connect java http while url invalid.");
            return null;
        }
        Headers headers=request.headers();
        HttpURLConnection finalConnection = null;
        try {
            HttpURLConnection connection=finalConnection= (HttpURLConnection) new URL(url).openConnection();
            //Add headers
            if (null!=headers){
                String key;String value=null;
                for (Map.Entry<String,String> child:headers.entrySet()) {
                    if (null==child||null==(key=child.getKey())){
                        continue;
                    }
                    value=headers.get(key);
                    connection.addRequestProperty(key,null!=value?value:"");
                }
            }
            connection.setRequestProperty("Charset","UTF-8");
            connection.setRequestProperty("Accept-Encoding","deflate");
            final long requestAtMillis=System.currentTimeMillis();
            connection.setRequestMethod(method.toUpperCase());
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            Debug.D("[Java http] Connect with "+method+" "+url);
            final Answer[] answers=new Answer[1];
            final OutputStream[] outputStreams=new OutputStream[1];
            final InputStream[] inputStreams=new InputStream[1];
            return new Connection() {
                @Override
                public Requested getRequested() {
                    return new Requested() {
                        @Override
                        public OutputStream getOutputStream() {
                            try {
                                if (null!=outputStreams[0]){
                                    return outputStreams[0];
                                }
                                return outputStreams[0]=connection.getOutputStream();
                            } catch (IOException e) {
                                Debug.E("Exception get java http output stream.e="+e);
                                e.printStackTrace();
                                return null;
                            }
                        }

                        @Override
                        public Answer getAnswer() {
                            if (null!=answers[0]){
                                return answers[0];
                            }
                            try {
                                final int responseCode=connection.getResponseCode();
                                final long responseAtMillis=System.currentTimeMillis();
                                final String responseMessage=connection.getResponseMessage();
                                final boolean followRedirects=connection.getInstanceFollowRedirects();
                                final long responseLength=connection.getContentLength();
                                final String contentType=connection.getContentType();
                                Map<String, List<String>> responseHeaderMap=connection.getHeaderFields();
                                final Headers responseHeaders=new Headers();
                                if (null!=responseHeaderMap){
                                    String childKey=null;List<String> childValue=null;
                                    StringBuffer buffer=new StringBuffer();
                                    for (Map.Entry<String,List<String>> child:responseHeaderMap.entrySet()) {
                                        if (null==(childKey=child.getKey())){
                                            continue;
                                        }else if (null!=(childValue=child.getValue())){
                                            buffer.delete(0,buffer.length());
                                            for (String child1:childValue){
                                                if (null!=child1){
                                                    buffer.append(child1);
                                                }
                                            }
                                            responseHeaders.add(childKey,buffer.toString());
                                        }
                                    }
                                }
                                return answers[0]=new Answer() {
                                    @Override
                                    public boolean isRedirect() {
                                        return followRedirects;
                                    }

                                    @Override
                                    public int getCode() {
                                        return responseCode;
                                    }

                                    @Override
                                    public AnswerBody getAnswerBody() {
                                        return new AnswerBody() {
                                            @Override
                                            public long getContentLength() {
                                                return responseLength;
                                            }

                                            @Override
                                            public String getContentType() {
                                                return contentType;
                                            }

                                            @Override
                                            public InputStream getInputStream() {
                                                try {
                                                    if (null!=inputStreams[0]){
                                                        return inputStreams[0];
                                                    }
                                                    return inputStreams[0]=connection.getInputStream();
                                                } catch (IOException e) {
                                                    Debug.E("Exception get http input stream.e="+e,e);
                                                    e.printStackTrace();
                                                    return null;
                                                }
                                            }
                                        };
                                    }

                                    @Override
                                    public String getMessage() {
                                        return responseMessage;
                                    }

                                    @Override
                                    public Headers getHeaders() {
                                        return responseHeaders;
                                    }

                                    @Override
                                    public long getReceivedResponseAtMillis() {
                                        return responseAtMillis;
                                    }

                                    @Override
                                    public long sentRequestAtMillis() {
                                        return requestAtMillis;
                                    }
                                };
                            } catch (IOException e) {
                                Debug.E("Exception get java http answer.e="+e,e);
                                e.printStackTrace();
                                return null;
                            }
                        }
                    };
                }

                @Override
                public void close() {
                    closes(outputStreams[0],inputStreams[0]);
                    connection.disconnect();
                }
            };
        } catch (IOException e) {
            Debug.E("Exception call java http.e="+e,e);
            if (null!=finalConnection){
                finalConnection.disconnect();
            }
            e.printStackTrace();
            return null;
        }
    }
//
//    @Override
//    protected Answer onCall(String method, String url, Request request) {
//        if (null==request){
//            Debug.E("Fail call java http while request invalid.");
//            return null;
//        }else if (null==method){
//            Debug.E("Fail call java http while method invalid.");
//            return null;
//        }else if (null==url||url.length()<=0){
//            Debug.E("Fail call java http while url invalid.");
//            return null;
//        }
//        Headers headers=request.headers();
//        Debug.D("[Java http]"+method+" "+url);
//        HttpURLConnection finalConnection;
//        Closeable finish=null;
//        OutputStream connectOutputStream=null;
//        try {
//            final HttpURLConnection connection=finalConnection= (HttpURLConnection) new URL(url).openConnection();
//            connection.setRequestProperty("Charset","UTF-8");
//            connection.setRequestProperty("Accept-Encoding","deflate");
////            connection.setRequestProperty("Connection", "Keep-Alive");
////            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
////            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
//            final Closeable finialFinish=finish=()-> {
//                if (null!=finalConnection){
//                    finalConnection.disconnect();
//                }
//            };
//            //Add headers
//            if (null!=headers){
//               String key;String value=null;
//                for (Map.Entry<String,String> child:headers.entrySet()) {
//                    if (null==child||null==(key=child.getKey())){
//                        continue;
//                    }
//                    value=headers.get(key);
//                    connection.addRequestProperty(key,null!=value?value:"");
//                }
//            }
//            //
//            final long requestAtMillis=System.currentTimeMillis();
//            OnOutputStreamOpen onOutputStreamOpen=request.getOnOutputStreamOpen();
//            connection.setRequestMethod(method.toUpperCase());
//            connection.setDoInput(true);
//            connection.setDoOutput(null!=onOutputStreamOpen);
//            connection.connect();
//            if (null!=onOutputStreamOpen){
//                onOutputStreamOpen.onOutputStreamOpen(connectOutputStream=connection.
//                        getOutputStream(),method,url,request);
//            }
//            final int responseCode=connection.getResponseCode();
//            final long responseAtMillis=System.currentTimeMillis();
//            final String responseMessage=connection.getResponseMessage();
//            final boolean followRedirects=connection.getInstanceFollowRedirects();
//            final long responseLength=connection.getContentLength();
//            final String contentType=connection.getContentType();
//            Map<String, List<String>> responseHeaderMap=connection.getHeaderFields();
//            final Headers responseHeaders=new Headers();
//            if (null!=responseHeaderMap){
//                String childKey=null;List<String> childValue=null;
//                StringBuffer buffer=new StringBuffer();
//                for (Map.Entry<String,List<String>> child:responseHeaderMap.entrySet()) {
//                    if (null==(childKey=child.getKey())){
//                        continue;
//                    }else if (null!=(childValue=child.getValue())){
//                        buffer.delete(0,buffer.length());
//                        for (String child1:childValue){
//                            if (null!=child1){
//                                buffer.append(child1);
//                            }
//                        }
//                        responseHeaders.add(childKey,buffer.toString());
//                    }
//                }
//            }
//            final InputStream finalInputStream=connection.getInputStream();
//            return new Answer() {
//                @Override
//                public boolean isRedirect() {
//                    return followRedirects;
//                }
//
//                @Override
//                public int getCode() {
//                    return responseCode;
//                }
//
//                @Override
//                public String getMessage() {
//                    return responseMessage;
//                }
//
//                @Override
//                public Headers getHeaders() {
//                    return responseHeaders;
//                }
//
//                @Override
//                public long getReceivedResponseAtMillis() {
//                    return responseAtMillis;
//                }
//
//                @Override
//                public long sentRequestAtMillis() {
//                    return requestAtMillis;
//                }
//
//                @Override
//                public AnswerBody getAnswerBody() {
//                    return new AnswerBody() {
//                        @Override
//                        public long getContentLength() {
//                            return responseLength;
//                        }
//
//                        @Override
//                        public String getContentType() {
//                            return contentType;
//                        }
//
//                        @Override
//                        public InputStream getInputStream() {
//                            return finalInputStream;
//                        }
//                    };
//                }
//
//            };
//        } catch (IOException e) {
//            Debug.E("Exception call java http.e="+e,e);
//            e.printStackTrace();
//            closes(finish,connectOutputStream);
//            return null;
//        }
//    }
}
