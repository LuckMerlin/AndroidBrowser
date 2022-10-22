package com.luckmerlin.task;

public interface TaskSaver {
    public interface TaskBytesReader{
        Task readTaskBytes(byte[] bytes);
    }
    boolean delete(Task task);
    void load(TaskBytesReader bytesReader);
    boolean write(Task task, byte[] taskBytes);
}
