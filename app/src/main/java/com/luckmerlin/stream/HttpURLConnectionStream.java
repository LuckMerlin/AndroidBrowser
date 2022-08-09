package com.luckmerlin.stream;


public class HttpURLConnectionStream extends URLConnectionStream{
    private final String mStreamUrl;

    public HttpURLConnectionStream(String streamUrl){
        mStreamUrl=streamUrl;
    }

    @Override
    protected HttpStreams onConnectionHttp(Convertor convertor) throws Exception {
        return new HttpStreams(createConnection(mStreamUrl),0,convertor).connect();
    }
}
