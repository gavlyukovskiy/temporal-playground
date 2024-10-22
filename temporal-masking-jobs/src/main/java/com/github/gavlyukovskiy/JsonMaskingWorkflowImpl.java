package com.github.gavlyukovskiy;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public class JsonMaskingWorkflowImpl implements JsonMaskingWorkflow {

    // Uses the default task queue shared by the pool of workers.
    private final JsonMaskingActivities activities;

    public JsonMaskingWorkflowImpl() {
        // Create activity clients.
        this.activities = Workflow.newActivityStub(JsonMaskingActivities.class, ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(10))
                .setRetryOptions(RetryOptions.newBuilder()
                                         .setInitialInterval(Duration.ofSeconds(1))
                                         .setMaximumAttempts(4)
                                         .setDoNotRetry(NonRetryableJsonException.class.getName())
                                         .build())
                .build());
    }

    @Override
    public String maskJson(String json) {
        RetryOptions retryOptions = RetryOptions.newBuilder().setInitialInterval(Duration.ofSeconds(1)).build();
        // Retries the whole sequence on any failure, potentially on a different host.
        return Workflow.retry(retryOptions, Optional.empty(), () -> maskJsonImpl(json));
    }

    private String maskJsonImpl(String json) {
        Set<String> maskingKeys = activities.getMaskingKeys(true);

        return activities.maskJson(json, maskingKeys);
    }
}
