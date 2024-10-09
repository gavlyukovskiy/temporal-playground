package com.github.gavlyukovskiy;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        logger.info("Temporal client 2 main");

        // gRPC stubs wrapper that talks to the local docker instance of temporal service.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        // client that can be used to start and signal workflows
        WorkflowClient client = WorkflowClient.newInstance(service);
        JsonMaskingWorkflow workflow = client.newWorkflowStub(
                JsonMaskingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(JsonMaskingWorkflow.TASK_QUEUE)
                        .build()
        );

        String json = """
                      {
                        "data": "public",
                        "secret": "super-secret-key"
                      }
                      """;

        logger.info("(sync) Executing JsonMaskingWorkflow");
        // This is going to block until the workflow completes.
        // This is rarely used in production. Use the commented code below for async start version.
        String masked = workflow.maskJson(json);
        logger.info("(sync) JsonMaskingWorkflow completed: %s".formatted(masked));

        logger.info("(async) Executing JsonMaskingWorkflow");
        WorkflowExecution workflowExecution = WorkflowClient.start(workflow::maskJson, json);
        logger.info("Started JsonMaskingWorkflow with workflowId='%s' and runId='%s'".formatted(
                workflowExecution.getWorkflowId(),
                workflowExecution.getRunId()
        ));
    }
}
