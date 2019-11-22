package controller.http

import data.http.api.*
import data.http.file.UploadResponse
import data.http.item.CreateItemRequest
import facade.addFileToItem
import facade.createAPIToken
import facade.createItemIndex
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.request.receiveStream
import io.ktor.routing.*
import service.*
import utils.WorkerNotificationChannels
import utils.api.apiRespond
import utils.api.authorizedUserId
import utils.api.get
import utils.zError

private fun ApplicationCall.getAPIToken(): String {
    return request.headers["ZHT-API-TOKEN"] ?: zError("Missing header: ZHT-API-TOKEN")
}

private fun ApplicationCall.userIdFromToken(): Long {
    val token = getAPIToken()
    return  getUserIdByAPIToken(token) ?:
            getUserIdByWorkerToken(token) ?:
            zError("Unknown token: '$token'")
}

private fun ApplicationCall.getWorkerIdFromToken(): Long {
    return getWorkerIdByToken(getAPIToken()) ?: zError("Unknown token")
}

private suspend fun notifyWorker(workerId: Long) {
    WorkerNotificationChannels.send(workerId)
}

fun Route.apiRouting(){
    route("token") {
        post("create") {
            val request = call.receive<GenerateAPITokenRequest>()
            call.apiRespond(createAPIToken(call.authorizedUserId, request.title))
        }

        get("query") {
            call.apiRespond(queryApiTokensByUserId(call.authorizedUserId))
        }

        delete("delete/{tokenId}") {
            val tokenId = call["tokenId"].toLong()
            deleteApiToken(call.authorizedUserId, tokenId)
            call.apiRespond()
        }
    }

    /**
     * POST /api/api/public-key
     */
    get("public-key") {
        val userId = call.userIdFromToken()
        call.apiRespond(getUserPublicKey(userId))
    }

    route("item") {
        post("add") {
            val userId = call.userIdFromToken()
            val request = call.receive<CreateItemRequest>()
            val itemId = createItemIndex(userId, request)
            call.apiRespond(UploadResponse(itemId))
        }
    }

    route("file") {
        put("upload/{itemId}/{encryptedFileName}") {
            val userId = call.userIdFromToken()
            val itemId = call["itemId"].toLong()
            val encryptedFileName = call["encryptedFileName"]
            val stream = call.receiveStream()
            addFileToItem(userId, itemId, encryptedFileName, stream)
            call.apiRespond()
        }
    }

    route("worker") {
        post("register") {
            val userId = call.userIdFromToken()
            val request = call.receive<WorkerRegisterRequest>()
            call.apiRespond(WorkerRegisterResponse(registerWorker(userId, request)))
        }
        get("query") {
            call.apiRespond(queryWorkers(call.authorizedUserId))
        }
        route("task") {
            post("add") {
                val userId = call.authorizedUserId
                val request = call.receive<WorkerAddTaskRequest>()
                addWorkerTask(userId, request)
                call.apiRespond()
                notifyWorker(request.workerId)
            }
            get("query") {
                call.apiRespond(queryWorkerTasks(call.authorizedUserId))
            }
            delete("poll") {
                call.apiRespond(PolledWorkerTask(pollWorkerTask(call.getWorkerIdFromToken())))
            }
            put("retry") {
                val (taskId) = call.receive<WorkerTaskStatusUpdateRequest>()
                val workerId = retryWorkerTask(call.authorizedUserId, taskId)
                call.apiRespond()
                notifyWorker(workerId)
            }
            route("status") {
                put("success") {
                    val workerId = call.getWorkerIdFromToken()
                    val (taskId) = call.receive<WorkerTaskStatusUpdateRequest>()
                    updateWorkerTaskStatus(workerId, taskId, WorkerTaskStatus.SUCCESS)
                    call.apiRespond()
                }
                put("failed") {
                    val workerId = call.getWorkerIdFromToken()
                    val (taskId) = call.receive<WorkerTaskStatusUpdateRequest>()
                    updateWorkerTaskStatus(workerId, taskId, WorkerTaskStatus.FAILED)
                    call.apiRespond()
                }
            }
        }

        delete("delete/{workerId}") {
            val workerId = call["workerId"].toLong()
            val userId = call.authorizedUserId
            deleteWorker(userId, workerId)
            call.apiRespond()
        }
    }
}