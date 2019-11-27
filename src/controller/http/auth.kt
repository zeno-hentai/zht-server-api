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
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import service.authorizeUser
import service.createUser
import service.getSaltByUsername
import service.getUserInfoByUserId
import utils.api.*
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
            salt = request.salt,
            publicKey = request.publicKey,
            encryptedPrivateKey = request.encryptedPrivateKey
        )
        call.userId = userId
        call.apiRespond(getUserInfoByUserId(userId))
    }

    get("salt/{username}") {
        val username = call["username"]
        call.apiRespond(getSaltByUsername(username))
    }

    post("login"){
        val request = call.receive<LoginRequest>()
        val userId = authorizeUser(
            username = request.username,
            password = request.password
        )
        call.userId = userId
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