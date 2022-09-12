package com.luckmerlin.http;

import java.io.OutputStream;

public interface OnOutputStreamOpen {
    void onOutputStreamOpen(OutputStream outputStream,String method, String url, Request request);
}
