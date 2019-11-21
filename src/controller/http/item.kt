package controller.http

import data.http.file.UploadResponse
import data.http.item.AddItemTagRequest
import data.http.item.CreateItemRequest
import data.http.item.UpdateEncryptedMetaRequest
import facade.addFileToItem
import facade.createItemIndex
import facade.deleteFileFromItem
import facade.deleteItem
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.request.receiveStream
import io.ktor.routing.*
import service.*
import utils.api.apiRespond
import utils.api.authorizedUserId
import utils.zError

fun Route.itemRouting() {

    get("paging") {
        call.apiRespond(getItemPagingOfUser(call.authorizedUserId))
    }

    get("get/{itemId}") {
        val itemId = call.parameters["itemId"]?.toLong() ?: zError("missing itemId")
        call.apiRespond(getItemData(
            call.authorizedUserId,
            itemId
        ))
    }

    post("create") {
        val request = call.receive<CreateItemRequest>()
        val itemId = createItemIndex(call.authorizedUserId, request)
        call.apiRespond(UploadResponse(itemId))
    }

    put("update/meta") {
        val request = call.receive<UpdateEncryptedMetaRequest>()
        updateItemEncryptedMeta(call.authorizedUserId, request.itemId, request.encryptedMeta)
        call.apiRespond()
    }

    get("query/{offset}/{limit}") {
        val offset = call.parameters["offset"]?.toInt() ?: zError("missing offset")
        val limit = call.parameters["limit"]?.toInt() ?: zError("missing limit")
        call.apiRespond(queryItemsOfUser(call.authorizedUserId, offset, limit))
    }

    delete("delete/{itemId}") {
        val itemId = call.parameters["itemId"]?.toLong() ?: zError("missing itemId")
        deleteItem(call.authorizedUserId, itemId)
        call.apiRespond(UploadResponse(itemId))
    }

    route("file") {
        put("upload/{itemId}/{encryptedFileName}") {
            val itemId = call.parameters["itemId"]?.toLong() ?: zError("missing itemId")
            val encryptedFileName = call.parameters["encryptedFileName"] ?: zError("missing encryptedFileName")
            val stream = call.receiveStream()
            addFileToItem(call.authorizedUserId, itemId, encryptedFileName, stream)
            call.apiRespond()
        }
        delete("delete/{itemId}/{mappedFileName}") {
            val itemId = call.parameters["itemId"]?.toLong() ?: zError("missing itemId")
            val mappedFileName = call.parameters["mappedFileName"] ?: zError("missing mappedFileName")
            deleteFileFromItem(call.authorizedUserId, itemId, mappedFileName)
            call.apiRespond()
        }
    }

    route("tag") {
        post("add") {
            val request = call.receive<AddItemTagRequest>()
            val response = addTagToItem(call.authorizedUserId, request.itemId, request.encryptedTag)
            call.apiRespond(response)
        }

        delete("delete/{tagId}") {
            val tagId = call.parameters["tagId"]?.toLong() ?: zError("missing itemId")
            deleteTagFromItem(call.authorizedUserId, tagId)
            call.apiRespond(Unit)
        }
    }
}