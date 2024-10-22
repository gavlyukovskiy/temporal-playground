package com.github.gavlyukovskiy;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public class FileUploadingWorkflowImpl implements FileUploadingWorkflow {

    // Uses the default task queue shared by the pool of workers.
    private final FileActivities fileActivities;
    private final JsonMaskingActivities jsonMaskingActivities;

    public FileUploadingWorkflowImpl() {
        // Create activity clients.
        this.fileActivities = createFileOperationActivities(null);
        this.jsonMaskingActivities = Workflow.newActivityStub(JsonMaskingActivities.class, ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(10))
                        .setRetryOptions(RetryOptions.newBuilder()
                                                 .setInitialInterval(Duration.ofSeconds(1))
                                                 .setMaximumAttempts(4)
                                                 .setDoNotRetry(NonRetryableJsonException.class.getName())
                                                 .build())
                        .build());
    }

    private static FileActivities createFileOperationActivities(String taskQueue) {
        var builder = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(30))
                .setRetryOptions(RetryOptions.newBuilder()
                                         .setInitialInterval(Duration.ofSeconds(1))
                                         .setMaximumAttempts(2)
                                         .build());
        if (taskQueue != null) {
            builder.setTaskQueue(taskQueue);
        }
        return Workflow.newActivityStub(FileActivities.class, builder.build());
    }

    @Override
    public String createAndUploadFile(String content) {
        RetryOptions retryOptions = RetryOptions.newBuilder().setInitialInterval(Duration.ofSeconds(1)).build();
        // Retries the whole sequence on any failure, potentially on a different host.
        return Workflow.retry(retryOptions, Optional.empty(), () -> createAndUploadFileImpl(content));
    }

    private String createAndUploadFileImpl(String content) {
        // TODO: this doesn't work for some reason?
        if (content.startsWith("{") && content.contains("secret")) {
            content = jsonMaskingActivities.maskJson(content, Set.of("secret"));
        }

        // this operation is executed on a random worker
        // but the response contains worker id that has that file created locally
        // which allows to continue operations based on that particular worker
        WorkerLocalFile localFile = fileActivities.createFile(content);

        // the file is stored locally on the specific worker
        // need to use that for any operations on it
        var workerSpecificFileActivities = createFileOperationActivities(localFile.workerId());

        workerSpecificFileActivities.processFile(localFile.path());
        if (workerSpecificFileActivities.uploadFile(localFile.path())) {
            // notification can be sent from any worker, so no need to use the worker specific queue
            fileActivities.sendNotification(localFile.id());
        }
        // going back to worker specific client for the cleanup
        workerSpecificFileActivities.deleteFile(localFile.path());

        return localFile.path();
    }
}
