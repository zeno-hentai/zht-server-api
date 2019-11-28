package service

import data.http.api.*
import model.APIToken
import model.RegisteredWorker
import model.User
import model.WorkerTask
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.WorkerNotificationChannels
import utils.assertAuthorized
import utils.maxValue
import utils.zError
import java.security.PublicKey

private fun Transaction.checkWorkerId(userId: Long, workerId: Long) {
    if((User innerJoin RegisteredWorker)
            .select { (RegisteredWorker.id eq workerId) and (User.id eq userId) }
            .count() == 0) {
        zError("unauthorized")
    }
}

fun registerWorker(userId: Long, request: WorkerRegisterRequest): Long = transaction {
    (User innerJoin APIToken).select {
        (User.id eq userId) and (APIToken.token eq request.token)
    }.assertAuthorized()
    val title = APIToken.select{ APIToken.token eq request.token }.first()[APIToken.title]
    APIToken.deleteWhere { APIToken.token eq request.token }
    RegisteredWorker.insert {
        it[RegisteredWorker.title] = title
        it[RegisteredWorker.token] = request.token
        it[RegisteredWorker.userId] = userId
    }
    RegisteredWorker.maxValue(RegisteredWorker.id) ?: zError("failed")
}

fun queryWorkers(userId: Long): List<WorkerInfoWithoutStatus> = transaction {
    (User innerJoin RegisteredWorker).select {
        User.id eq userId
    }
        .orderBy(RegisteredWorker.id, SortOrder.DESC)
        .map {
            val workerId = it[RegisteredWorker.id]
            WorkerInfoWithoutStatus(
                id = workerId,
                title = it[RegisteredWorker.title]
            )
        }
}

fun getWorker(userId: Long, workerId: Long): WorkerInfoWithoutStatus = transaction {
    (User innerJoin RegisteredWorker).select {
        (User.id eq userId) and
                (RegisteredWorker.id eq workerId)
    }.assertAuthorized()
    val result = RegisteredWorker.select {
        RegisteredWorker.id eq workerId
    }.first()
    val workerId = result[RegisteredWorker.id]
    WorkerInfoWithoutStatus(
        id = workerId,
        title = result[RegisteredWorker.title]
    )
}

fun getWorkerIdByToken(token: String): Long? = transaction {
    RegisteredWorker.select {
        RegisteredWorker.token eq token
    }.firstOrNull()?.get(RegisteredWorker.id)
}

fun addWorkerTask(userId: Long, request: WorkerAddTaskRequest): Long = transaction {
    if((User innerJoin RegisteredWorker).select { (User.id eq userId) and (RegisteredWorker.id eq request.workerId) }.count() == 0){
        zError("unauthorized")
    }
    WorkerTask.insert {
        it[WorkerTask.workerId] = request.workerId
        it[WorkerTask.status] = WorkerTaskStatus.SUSPENDED
        it[WorkerTask.encryptedURLToUser] = request.encryptedURLToUser
        it[WorkerTask.encryptedURLToWorker] = request.encryptedURLToWorker
    }
    WorkerTask.maxValue(WorkerTask.id) ?: zError("Failed to create")
}

fun queryWorkerTasks(userId: Long): List<WorkerTaskInfo> = transaction {
    (User innerJoin RegisteredWorker innerJoin WorkerTask).select {
        User.id eq userId
    }
        .orderBy(WorkerTask.id, SortOrder.DESC)
        .map{
            WorkerTaskInfo(
                id = it[WorkerTask.id],
                workerId = it[RegisteredWorker.id],
                workerTitle = it[RegisteredWorker.title],
                encryptedURL = it[WorkerTask.encryptedURLToUser],
                status = it[WorkerTask.status]
            )
        }
}

fun cancelAllTasks(workerId: Long) = transaction {
    WorkerTask.update({WorkerTask.workerId eq workerId}) {
        it[WorkerTask.status] = WorkerTaskStatus.FAILED
    }
}

fun getWorkerTask(userId: Long, taskId: Long): WorkerTaskInfo = transaction {
    (User innerJoin RegisteredWorker innerJoin WorkerTask).select {
        (User.id eq userId) and (WorkerTask.id eq taskId)
    }.assertAuthorized()
    val result = (RegisteredWorker innerJoin WorkerTask)
        .select { WorkerTask.id eq taskId }
        .first()
    WorkerTaskInfo(
        id = result[WorkerTask.id],
        workerId = result[RegisteredWorker.id],
        workerTitle = result[RegisteredWorker.title],
        encryptedURL = result[WorkerTask.encryptedURLToUser],
        status = result[WorkerTask.status]
    )
}

fun deleteWorkerTask(userId: Long, taskId: Long) = transaction {
    (User innerJoin RegisteredWorker innerJoin WorkerTask)
        .select {
            (User.id eq userId) and
                    (WorkerTask.id eq taskId)
        }.assertAuthorized()
    if(WorkerTask.select {
            (WorkerTask.id eq taskId) and
                    (WorkerTask.status eq WorkerTaskStatus.RUNNING)
        }.count() != 0) {
        zError("Task is running.")
    }
    WorkerTask.deleteWhere { WorkerTask.id eq taskId }
}

fun pollWorkerTask(workerId: Long): WorkerTaskInfo? = transaction {
    WorkerTask.update({
        (WorkerTask.workerId eq workerId) and (WorkerTask.status eq WorkerTaskStatus.RUNNING)
    }) {
        it[WorkerTask.status] = WorkerTaskStatus.FAILED
    }
    val result = (RegisteredWorker innerJoin WorkerTask).select {
        (RegisteredWorker.id eq workerId) and
                (WorkerTask.status eq WorkerTaskStatus.SUSPENDED)
    }.orderBy(WorkerTask.id, SortOrder.ASC)
        .firstOrNull()?.let {
            WorkerTaskInfo(
                id = it[WorkerTask.id],
                workerId = it[RegisteredWorker.id],
                workerTitle = it[RegisteredWorker.title],
                encryptedURL = it[WorkerTask.encryptedURLToWorker],
                status = it[WorkerTask.status]
            )
        }
    result?.let { (id) ->
        WorkerTask.update({WorkerTask.id eq id}) {
            it[WorkerTask.status] = WorkerTaskStatus.RUNNING
        }
    }
    result
}

fun deleteWorker(userId: Long, workerId: Long) = transaction {
    checkWorkerId(userId, workerId)
    WorkerTask.deleteWhere { WorkerTask.workerId eq workerId }
    RegisteredWorker.deleteWhere { RegisteredWorker.id eq workerId }
}

fun updateWorkerTaskStatus(workerId: Long, taskId: Long, status: WorkerTaskStatus) = transaction {
    (RegisteredWorker innerJoin WorkerTask).select {
            (RegisteredWorker.id eq workerId) and
                    (WorkerTask.id eq taskId)
        }.assertAuthorized()
    WorkerTask.select {
        (WorkerTask.id eq taskId)
    }.firstOrNull()?.let {
        if(it[WorkerTask.status] != WorkerTaskStatus.RUNNING){
            zError("Task not running: ${it[WorkerTask.status]}")
        }
    }
    WorkerTask.update({WorkerTask.id eq taskId}) {
        it[WorkerTask.status] = status
    }
}