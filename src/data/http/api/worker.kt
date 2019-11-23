package data.http.api

enum class WorkerTaskStatus {
    SUSPENDED,
    RUNNING,
    SUCCESS,
    FAILED
}

data class WorkerRegisterRequest(
    val token: String,
    val encryptedPublicKey: String
)

data class WorkerTaskStatusUpdateRequest(
    val taskId: Long
)

data class WorkerRegisterResponse(
    val id: Long
)

data class WorkerAddTaskRequest(
    val workerId: Long,
    val encryptedURLToWorker: String,
    val encryptedURLToUser: String
)

data class WorkerInfo (
    val id: Long,
    val title: String,
    val encryptedPublicKey: String,
    val online: Boolean
)

data class WorkerTaskInfo (
    val id: Long,
    val workerId: Long,
    val workerTitle: String,
    val encryptedURL: String,
    val status: WorkerTaskStatus
)

data class PolledWorkerTask(val data: WorkerTaskInfo?){
    val hasTask = data != null
}