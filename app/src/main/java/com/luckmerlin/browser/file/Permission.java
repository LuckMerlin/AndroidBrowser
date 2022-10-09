package com.luckmerlin.browser.file;

public interface Permission {
    int PERMISSION_NONE=0;
    int PERMISSION_READ=0b01;
    int PERMISSION_WRITE=0b10;
    int PERMISSION_EXECUTE=0b100;
}
