package com.luckmerlin.stream;

import java.io.Closeable;

public interface Stream extends Closeable {
    InputStream openInputStream(long skip) throws Exception;
    OutputStream openOutputStream()throws Exception;
}
