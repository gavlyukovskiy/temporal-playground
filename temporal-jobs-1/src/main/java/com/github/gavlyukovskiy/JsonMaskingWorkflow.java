package com.github.gavlyukovskiy;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface JsonMaskingWorkflow {
    String TASK_QUEUE = "JsonMaskingWorkflow";
    @WorkflowMethod
    String maskJson(String json);
}
