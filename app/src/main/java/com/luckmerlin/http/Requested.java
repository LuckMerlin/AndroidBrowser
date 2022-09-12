package com.luckmerlin.http;

import java.io.OutputStream;

public interface Requested {
    OutputStream getOutputStream();
    Answer getAnswer();
}
