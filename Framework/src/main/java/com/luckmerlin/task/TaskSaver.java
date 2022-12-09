package com.luckmerlin.task;

public interface TaskSaver {

   boolean delete(Object obj);

    public interface OnTaskLoad{
        void onTaskLoaded(String taskId,byte[] bytes);
    }

    void load(OnTaskLoad onTaskLoad);

    boolean write(String taskId, byte[] taskBytes);
}
