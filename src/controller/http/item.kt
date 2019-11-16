package controller.http

import data.http.item.AddItemTagRequest
import facade.deleteItem
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
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

    get("query/{offset}/{limit}") {
        val offset = call.parameters["offset"]?.toInt() ?: zError("missing offset")
        val limit = call.parameters["limit"]?.toInt() ?: zError("missing limit")
        call.apiRespond(queryItemsOfUser(call.authorizedUserId, offset, limit))
    }

    delete("delete/{itemId}") {
        val itemId = call.parameters["itemId"]?.toLong() ?: zError("missing itemId")
        deleteItem(call.authorizedUserId, itemId)
        call.apiRespond(Unit)
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