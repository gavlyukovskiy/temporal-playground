package com.github.gavlyukovskiy;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface FileUploadingWorkflow {
    String TASK_QUEUE = FileUploadingWorkflow.class.getSimpleName();

    @WorkflowMethod
    String createAndUploadFile(String content);
}
