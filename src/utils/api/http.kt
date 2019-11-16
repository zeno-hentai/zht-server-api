package utils.api

import io.ktor.application.ApplicationCall
import io.ktor.response.respond

data class ZhtApiResponse<T>(val data: T)

suspend fun <T> ApplicationCall.apiRespond(data: T) =
    respond(ZhtApiResponse(data))