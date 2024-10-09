Start docker compose

```bash
docker compose up -d
```

Run workers and client.

## Notes
### Configuration

When something is misconfigured we get a Go stacktrace without much indication about what went wrong.

### Docker compose

A lot of docker composes are available (https://github.com/temporalio/docker-compose). 
However while simplified setup (`auto-setup`) seems to work fine, setting up multi-role with multiple workers is quite cumbersome. 

### Worker polling problem

After starting a worker it cannot connect to the cluster with the error
```text
23:51:27.129 [Workflow Poller taskQueue="JsonMaskingWorkflow", namespace="default": 2] WARN  io.temporal.internal.worker.Poller - Failure in poller thread Workflow Poller taskQueue="JsonMaskingWorkflow", namespace="default": 2
io.grpc.StatusRuntimeException: UNAVAILABLE: Not enough hosts to serve the request
```

Same is in docker logs
```json
{"level":"error","ts":"2024-10-09T22:06:55.809Z","msg":"Unable to call matching.PollActivityTaskQueue.","service":"frontend","wf-task-queue-name":"/_sys/JsonMaskingWorkflow/2","timeout":"1m9.999500167s","error":"Not enough hosts to serve the request","logging-call-at":"/home/runner/work/docker-builds/docker-builds/temporal/service/frontend/workflow_handler.go:1094",...
```

Submitting the workflow worked, but going inside the workflow gives 503 error.

Turns out to be the problem of misconfigured database (`DB=postgresql`).

### Activity reporting problem

After finishing a workflow got this error on the worker
```text
00:11:40.867 [Activity Executor taskQueue="ae7cb9ff-dc2b-430a-8f02-77e56627c429", namespace="default": 1] WARN  io.temporal.internal.worker.ActivityWorker - Failure during reporting of activity result to the server. ActivityId = 7ecdef9e-9a39-35ef-8576-58cfae2183f6, ActivityType = MaskJson, WorkflowId=fb3107ed-9296-4c6e-a6a3-34d4c5c4983f, WorkflowType=JsonMaskingWorkflow, RunId=a069d15b-62d5-4cd8-8303-85900f9bd49c
io.grpc.StatusRuntimeException: NOT_FOUND: invalid activityID or activity already timed out or invoking workflow is completed
```

client never received an update.

### Module structure

Not quite clear what code needs to live where. Potentially the client if fully decouples from the worker, but what exactly needs to be shared between the client and the worker?
- workflow interfaces
- workflow implementation
- activity interfaces
- activity implementation

### Specific queues

Quite interesting that we have a way to continue the activity on a specific worker (https://github.com/temporalio/samples-java/tree/main/core/src/main/java/io/temporal/samples/fileprocessing)
not clear what happens if the worker dies mid execution. Do we need to use persistent IDs for the worker so that the workflow can continue?

### Examples

Quite a lot of examples available: https://github.com/temporalio/samples-java

### Data sensitivity

The job inputs / outputs must not contain anything sensitive :)

