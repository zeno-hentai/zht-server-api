package utils.api

import io.ktor.application.ApplicationCall
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import utils.zError
import java.io.Serializable

data class ZHTSession(val userId: Long?): Serializable

val ApplicationCall.userId: Long?
    get() {
        return sessions.get<ZHTSession>()?.userId
    }

val ApplicationCall.authorizedUserId: Long
    get() = userId ?: zError("Not Login")