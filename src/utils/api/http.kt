package utils.api

import io.ktor.application.ApplicationCall
import io.ktor.response.respond
import utils.zError

abstract class ZhtApiResponse(val success: Boolean)

class ZhtApiSuccessResponse<T>(val data: T): ZhtApiResponse(true)
class ZhtApiErrorResponse(val error: String?): ZhtApiResponse(false)

suspend fun ApplicationCall.apiRespond() = apiRespond(Unit)

suspend fun <T> ApplicationCall.apiRespond(data: T) =
    respond(ZhtApiSuccessResponse(data))

operator fun ApplicationCall.get(name: String): String =
    parameters[name] ?: zError("missing $name")