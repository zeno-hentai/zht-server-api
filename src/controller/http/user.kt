package controller.http

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import service.getUserInfoByUserId
import service.queryLatestUpdateTime
import utils.api.apiRespond
import utils.api.authorizedUserId
import utils.api.userId

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