package controller.http

import facade.deleteUser
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import service.getUserInfoByUserId
import service.queryLatestUpdateTime
import utils.api.apiRespond
import utils.api.authorizedUserId
import utils.api.userId
import utils.zError

fun Route.userRouting() {
    get("info") {
        val userInfo = call.userId?.let {
            getUserInfoByUserId(it)
        }
        call.apiRespond(userInfo)
    }

    get("update") {
        call.apiRespond(queryLatestUpdateTime(call.authorizedUserId))
    }
}