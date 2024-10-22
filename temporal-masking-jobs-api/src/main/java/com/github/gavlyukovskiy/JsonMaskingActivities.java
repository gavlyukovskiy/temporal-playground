package com.github.gavlyukovskiy;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.Set;

@ActivityInterface
public interface JsonMaskingActivities {
    @ActivityMethod
    Set<String> getMaskingKeys(boolean strict);

    @ActivityMethod
    String maskJson(String json, Set<String> maskingKeys);
}
