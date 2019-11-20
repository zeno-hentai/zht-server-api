package controller.http

import facade.transferFileFromFileManager
import io.ktor.application.call
import io.ktor.response.respondOutputStream
import io.ktor.routing.Route
import io.ktor.routing.get
import service.authorizeFile
import service.getFileNamesByItemId
import utils.api.apiRespond
import utils.api.authorizedUserId
import utils.zError

fun Route.fileRouting(){
    /**
     * GET /api/file/data/{itemId}/{name}
     */
    get("data/{itemId}/{name}") {
        val itemId = call.parameters["itemId"]?.toLong() ?: zError("Misssing itemId")
        val name = call.parameters["name"] ?: zError("Empty Filename")
        val userId = call.authorizedUserId
        if(!authorizeFile(userId, itemId, name)){
            zError("Failed to authorize file.")
        }
        call.respondOutputStream {
            transferFileFromFileManager(name, this)
        }
    }

    /**
     * GET /api/file/list/{itemId}
     */
    get("list/{itemId}") {
        val itemId = call.parameters["itemId"]?.toLong() ?: zError("missing itemId")
        call.apiRespond(getFileNamesByItemId(call.authorizedUserId, itemId))
    }
}