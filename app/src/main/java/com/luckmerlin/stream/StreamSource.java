package com.luckmerlin.stream;

import java.io.Closeable;

public interface StreamSource extends Closeable {
    InputStream openInputStream(long skip,Convertor convertor) throws Exception;
    OutputStream openOutputStream(Convertor convertor)throws Exception;
}
