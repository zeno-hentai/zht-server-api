package controller.http

import config.ZHTConfig
import data.http.auth.LoginRequest
import data.http.auth.MasterKeyRequest
import data.http.auth.RegisterRequest
import facade.deleteUser
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.post
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import service.authorizeUser
import service.createUser
import service.getUserInfoByUserId
import utils.api.ZHTSession
import utils.api.apiRespond
import utils.api.authorizedUserId
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
            password = request.password
        )
        call.sessions.set(ZHTSession(
            userId = userId
        ))
        call.apiRespond(getUserInfoByUserId(userId))
    }

    delete("logout"){
        call.sessions.clear<ZHTSession>()
        call.apiRespond("success")
    }

    delete("delete") {
        val userId = call.authorizedUserId
        deleteUser(userId)
        call.sessions.clear<ZHTSession>()
        call.apiRespond("success")
    }
}