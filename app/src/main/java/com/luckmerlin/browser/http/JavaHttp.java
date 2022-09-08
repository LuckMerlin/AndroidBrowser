package com.luckmerlin.browser.http;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.AnswerBody;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Answer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class JavaHttp extends Http {

    @Override
    protected Answer onCall(String method, String url, Request request) {
        if (null==request){
            Debug.E("Fail call java http while request invalid.");
            return null;
        }else if (null==method){
            Debug.E("Fail call java http while method invalid.");
            return null;
        }else if (null==url||url.length()<=0){
            Debug.E("Fail call java http while url invalid.");
            return null;
        }
        Headers headers=request.headers();
        Debug.D("[Java http]"+method+" "+url);
        HttpURLConnection connection=null;
        Closeable finish=null;
        try {
            final HttpURLConnection finalConnection=connection= (HttpURLConnection) new URL(url).openConnection();
            final Closeable finialFinish=finish=()-> {
                if (null!=finalConnection){
                    finalConnection.disconnect();
                }
            };
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
            //
            final long requestAtMillis=System.currentTimeMillis();
            connection.setRequestMethod(method.toUpperCase());
            connection.setDoInput(true);
            connection.connect();
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
                for (Map.Entry<String,List<String>> child:responseHeaderMap.entrySet()) {
                    if (null==(childKey=child.getKey())){
                        continue;
                    }
                    childValue=child.getValue();
                    responseHeaders.add(childKey,childValue);
                }
            }
            final InputStream finalInputStream=connection.getInputStream();
            return new Answer() {
                @Override
                public boolean isRedirect() {
                    return followRedirects;
                }

                @Override
                public int getCode() {
                    return responseCode;
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

                @Override
                public AnswerBody getResponseBody() {
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
                        public InputStream getStream() {
                            return finalInputStream;
                        }

                        @Override
                        public boolean close()  {
                            closes(finialFinish);
                            return true;
                        }
                    };
                }

            };
        } catch (IOException e) {
            Debug.E("Exception call java http.e="+e,e);
            e.printStackTrace();
            closes(finish);
            return null;
        }
    }
}
