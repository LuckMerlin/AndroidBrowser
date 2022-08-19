package com.luckmerlin.task;

public interface TaskSaver {
    public interface TaskBytesReader{
        Task readTaskBytes(byte[] bytes);
    }
    void load(TaskBytesReader bytesReader);
    boolean write(Task task, byte[] taskBytes);
}
