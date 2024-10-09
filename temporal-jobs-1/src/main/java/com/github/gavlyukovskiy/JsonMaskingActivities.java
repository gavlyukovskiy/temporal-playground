package com.github.gavlyukovskiy;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface JsonMaskingActivities {
    @ActivityMethod
    String maskJson(String json);

    @ActivityMethod
    String getCurrentWorkerQueue();
}
