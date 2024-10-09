package com.github.gavlyukovskiy;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Optional;

public class JsonMaskingWorkflowImpl implements JsonMaskingWorkflow {

    // Uses the default task queue shared by the pool of workers.
    private final JsonMaskingActivities activities;

    public JsonMaskingWorkflowImpl() {
        // Create activity clients.
        ActivityOptions ao = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(20))
                .setRetryOptions(RetryOptions.newBuilder()
                                         .setInitialInterval(Duration.ofSeconds(1))
                                         .setMaximumAttempts(4)
                                         .setDoNotRetry(IllegalArgumentException.class.getName())
                                         .build())
                .build();
        this.activities = Workflow.newActivityStub(JsonMaskingActivities.class, ao);
    }

    @Override
    public String maskJson(String json) {
        RetryOptions retryOptions = RetryOptions.newBuilder().setInitialInterval(Duration.ofSeconds(1)).build();
        // Retries the whole sequence on any failure, potentially on a different host.
        return Workflow.retry(retryOptions, Optional.empty(), () -> maskJsonImpl(json));
    }

    private String maskJsonImpl(String json) {
        // executed on random worker
        String workerQueue = activities.getCurrentWorkerQueue();

        // Now initialize stubs that are specific to the returned task queue.
        ActivityOptions hostActivityOptions = ActivityOptions.newBuilder()
                .setTaskQueue(workerQueue)
                // Set the amount a time an activity task can stay in the task queue before its picked
                // up by a Worker. It allows us to support cases where
                // the activity worker crashes or restarts before the activity starts execution.
                // This timeout should be specified only when host specific activity task queues are
                // used like in this sample.
                // Note that scheduleToStart timeout is not retryable and retry options will ignore it.
                // This timeout has to be handled by Workflow code.
                .setScheduleToStartTimeout(Duration.ofSeconds(30))
                // Set the max time of a single activity execution attempt.
                // Activity is going to be executed by a Worker listening to the specified
                // host task queue. If the activity is started but then the activity worker crashes
                // for some reason, we want to make sure that it is retried after the specified timeout.
                // This timeout should be as short as the longest possible execution of the Activity.
                .setStartToCloseTimeout(Duration.ofSeconds(10))
                .setRetryOptions(RetryOptions.newBuilder()
                                         .setInitialInterval(Duration.ofSeconds(1))
                                         .setMaximumAttempts(4)
                                         .setDoNotRetry(IllegalArgumentException.class.getName())
                                         .build()
                )
                .build();

        JsonMaskingActivities workerSpecificActivities = Workflow.newActivityStub(
                JsonMaskingActivities.class,
                hostActivityOptions
        );

        String maskedBySpecificWorker = workerSpecificActivities.maskJson(json);
        String maskedByRandomWorker = activities.maskJson(json);

        if (maskedBySpecificWorker.length() != maskedByRandomWorker.length()) {
            throw new IllegalStateException("JSONs don't match");
        }

        return maskedBySpecificWorker;
    }
}
