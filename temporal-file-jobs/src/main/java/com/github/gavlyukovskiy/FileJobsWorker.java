package com.github.gavlyukovskiy;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.util.UUID;

public class FileJobsWorker {

    public static void main(String[] args) {
        String workerId = UUID.randomUUID().toString();

        // gRPC stubs wrapper that talks to the local docker instance of temporal service.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        // client that can be used to start and signal workflows
        WorkflowClient client = WorkflowClient.newInstance(service);

        // worker factory that can be used to create workers for specific task queues
        WorkerFactory factory = WorkerFactory.newInstance(client);
        // Worker that listens on a task queue and hosts both workflow and activity implementations.
        Worker workerForCommonTaskQueue = factory.newWorker(FileUploadingWorkflow.TASK_QUEUE);
        workerForCommonTaskQueue.registerWorkflowImplementationTypes(FileUploadingWorkflowImpl.class);
        var fileActivities = new FileActivitiesImpl(workerId);
        workerForCommonTaskQueue.registerActivitiesImplementations(fileActivities);

        // Get worker to poll the host-specific task queue.
        Worker workerForHostSpecificTaskQueue = factory.newWorker(workerId);
        workerForHostSpecificTaskQueue.registerActivitiesImplementations(fileActivities);

        // Start all workers created by this factory.
        factory.start();
        System.out.println("Worker started for task queue: " + FileUploadingWorkflow.TASK_QUEUE);
        System.out.println("Worker Started for activity task Queue: " + workerId);
    }
}
