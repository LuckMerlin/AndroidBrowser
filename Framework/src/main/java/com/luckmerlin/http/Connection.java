package com.luckmerlin.http;

import java.io.Closeable;

public interface Connection extends Closeable {
    Requested getRequested();
}
