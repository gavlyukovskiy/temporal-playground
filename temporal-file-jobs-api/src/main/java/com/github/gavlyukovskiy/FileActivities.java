package com.github.gavlyukovskiy;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface FileActivities {

    @ActivityMethod
    WorkerLocalFile createFile(String content);

    @ActivityMethod
    void processFile(String path);

    @ActivityMethod
    boolean uploadFile(String path);

    @ActivityMethod
    void deleteFile(String path);

    @ActivityMethod
    void sendNotification(String path);
}
