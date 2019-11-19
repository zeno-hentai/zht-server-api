package controller.http

import data.http.user.UserUnauthorizedResponse
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
        } ?: UserUnauthorizedResponse()
        call.apiRespond(userInfo)
    }

    get("update") {
        call.apiRespond(queryLatestUpdateTime(call.authorizedUserId))
    }
}