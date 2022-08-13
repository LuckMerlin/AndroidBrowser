package com.luckmerlin.browser.task;

import com.luckmerlin.browser.file.File;
import com.luckmerlin.stream.Convertor;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.stream.StreamSource;
import java.io.IOException;

public class FileStream implements StreamSource {
    public final File mFile;

    public FileStream(File file){
        mFile=file;
    }

    @Override
    public InputStream openInputStream(long skip, Convertor convertor) throws Exception {
        return null;
    }

    @Override
    public OutputStream openOutputStream(Convertor convertor) throws Exception {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
