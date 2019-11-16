package controller.http

import config.ZHTConfig
import data.http.auth.LoginRequest
import data.http.auth.MasterKeyRequest
import data.http.auth.RegisterRequest
import facade.deleteUser
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.post
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import service.authorizeUser
import utils.api.ZHTSession
import io.ktor.sessions.set
import service.createUser
import service.getUserInfoByUserId
import utils.api.apiRespond
import utils.api.userId
import utils.zError

fun MasterKeyRequest.checkMasterKey () {
    if(masterKey != ZHTConfig.apiAdminSecret) {
        zError("invalid masterKey")
    }
}

fun Route.authRouting(){
    post("register"){
        val request = call.receive<RegisterRequest>()
        request.checkMasterKey()
        val userId = createUser(
            username = request.username,
            password = request.password,
            publicKey = request.publicKey,
            encryptedPrivateKey = request.encryptedPrivateKey
        )
        call.sessions.set(ZHTSession(userId))
        call.apiRespond(getUserInfoByUserId(userId))
    }

    post("login"){
        val request = call.receive<LoginRequest>()
        val userId = authorizeUser(
            username = request.username,
            password = request.username
        )
        call.sessions.set(ZHTSession(
            userId = userId
        ))
        call.apiRespond(getUserInfoByUserId(userId))
    }

    post("logout"){
        call.sessions.clear<ZHTSession>()
        call.apiRespond("success")
    }

    delete("delete/{userId}") {
        val userId = call.userId ?: zError("Unauthorized")
        deleteUser(userId)
        call.apiRespond(Unit)
    }
}