package com.luckmerlin.core;

public interface ChangeUpdate {
    boolean addChangeListener(OnChangeUpdate changeUpdate);
    boolean removeChangeListener(OnChangeUpdate changeUpdate);
}
