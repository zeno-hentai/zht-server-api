package controller.http

import facade.transferFileFromFileManager
import facade.unpackResourceFile
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondOutputStream
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import service.authorizeFile
import service.getUserIdByAPIToken
import service.getUserPublicKey
import utils.api.authorizedUserId
import utils.zError

private fun ApplicationCall.userIdFromToken(): Long {
    val token = request.headers["ZHT-API-TOKEN"] ?: zError("Missing header: ZHT-API-TOKEN")
    return getUserIdByAPIToken(token) ?: zError("Unknown token.")
}

fun Route.fileRouting(){
    /**
     * POST /api/file/user/public-key
     */
    post("user/public-key") {
        val userId = call.userIdFromToken()
        call.respond(getUserPublicKey(userId))
    }

    /**
     * POST /api/file/item/upload
     */
    post("item/upload") {
        val userId = call.userIdFromToken()
        val stream = call.receiveStream()
        unpackResourceFile(userId, stream)
    }

    /**
     * GET /api/file/data/{name}
     */
    get("data/{name}") {
        val name = call.parameters["name"] ?: zError("Empty Filename")
        val userId = call.authorizedUserId
        if(!authorizeFile(userId, name)){
            zError("Failed to authorize file.")
        }
        call.respondOutputStream {
            transferFileFromFileManager(name, this)
        }
    }
}