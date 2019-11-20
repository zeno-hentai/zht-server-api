package controller.http

import data.http.api.GenerateAPITokenRequest
import data.http.file.UploadResponse
import data.http.item.CreateItemRequest
import facade.addFileToItem
import facade.createAPIToken
import facade.createItemIndex
import facade.unpackResourceFile
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.request.receiveStream
import io.ktor.routing.*
import service.deleteApiToken
import service.getUserIdByAPIToken
import service.getUserPublicKey
import service.queryApiTokensByUserId
import utils.api.apiRespond
import utils.api.authorizedUserId
import utils.zError

private fun ApplicationCall.userIdFromToken(): Long {
    val token = request.headers["ZHT-API-TOKEN"] ?: zError("Missing header: ZHT-API-TOKEN")
    return getUserIdByAPIToken(token) ?: zError("Unknown token: '$token'")
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
            val tokenId = call.parameters["tokenId"]?.toLong() ?: zError("missing tokenId")
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

    /**
     * POST /api/api/upload
     */
//    post("upload") {
//        val userId = call.userIdFromToken()
//        val stream = call.receiveStream()
//        val itemId = unpackResourceFile(userId, stream)
//        call.apiRespond(UploadResponse(itemId))
//    }

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
            val itemId = call.parameters["itemId"]?.toLong() ?: zError("missing itemId")
            val encryptedFileName = call.parameters["encryptedFileName"] ?: zError("missing encryptedFileName")
            val stream = call.receiveStream()
            addFileToItem(userId, itemId, encryptedFileName, stream)
            call.apiRespond()
        }
    }
}