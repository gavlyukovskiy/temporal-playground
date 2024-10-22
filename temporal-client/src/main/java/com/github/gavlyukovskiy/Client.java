package com.github.gavlyukovskiy;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Scanner;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        // gRPC stubs wrapper that talks to the local docker instance of temporal service.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        // client that can be used to start and signal workflows
        WorkflowClient client = WorkflowClient.newInstance(service);

        while (!Thread.currentThread().isInterrupted()) {
            logger.info("Enter 'mask|upload <content>':");

            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            String[] commands = command.split(" ", 2);
            if (commands.length != 2) {
                logger.error("Invalid command");
                continue;
            }

            String content = commands[1];
            switch (commands[0]) {
                case "mask" -> {
                    JsonMaskingWorkflow syncWorkflow = jsonMaskingWorkflow(client);
                    logger.info("Scheduling JsonMaskingWorkflow...");
                    // This is going to block until the syncWorkflow completes.
                    // This is rarely used in production. Use the commented code below for async start version.
                    String masked = syncWorkflow.maskJson(content);
                    logger.info("JsonMaskingWorkflow completed:\n%s".formatted(masked));
                }
                case "upload" -> {
                    var workflow = fileUploadingWorkflow(client);
                    logger.info("Scheduling FileUploadingWorkflow...");
                    // This is going to block until the workflow completes.
                    // Blocking isn't a very good idea, which is why there's an async client as well
                    String path = workflow.createAndUploadFile(content);
                    logger.info("FileUploadingWorkflow completed:\n%s".formatted(path));
                }
                default -> logger.error("Invalid command");
            }
        }
    }

    private static FileUploadingWorkflow fileUploadingWorkflow(WorkflowClient client) {
        return client.newWorkflowStub(
                FileUploadingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(FileUploadingWorkflow.TASK_QUEUE)
                        .build()
        );
    }

    private static JsonMaskingWorkflow jsonMaskingWorkflow(WorkflowClient client) {
        return client.newWorkflowStub(
                JsonMaskingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(JsonMaskingWorkflow.TASK_QUEUE)
                        .build()
        );
    }
}
