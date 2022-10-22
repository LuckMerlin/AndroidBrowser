package com.luckmerlin.http;

import java.io.InputStream;

public interface RequestBody {
    InputStream openInputStream(String method, String url, Request request, Http http);
    String getMediaType();
    void close();
}
