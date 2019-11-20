package utils.api

import config.SessionKVService
import io.ktor.application.ApplicationCall
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import utils.zError
import java.io.Serializable
import java.util.*

data class ZHTSession(val token: String?): Serializable {
    constructor(): this(null)
}

var ApplicationCall.userId: Long?
    get() {
        return sessions.get<ZHTSession>()?.token?.let{
            SessionKVService[it]?.toLong()
        }
    }
    set(value) {
        if(value == null){
            sessions.set(ZHTSession(null))
        }else{
            sessions.get<ZHTSession>()?.token?.let {
                SessionKVService.delete(it)
            }
            val token = UUID.randomUUID().toString()
            SessionKVService[token] = value.toString()
            sessions.set(ZHTSession(token))
        }
    }

val ApplicationCall.authorizedUserId: Long
    get() = userId ?: zError("Not Login")