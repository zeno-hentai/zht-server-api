package service

import config.ZHTConfig
import data.http.user.UserInformationResponse
import model.APIToken
import model.RegisteredWorker
import model.User
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.crypto.bcrypt.BCrypt
import utils.maxValue
import utils.zError

fun createUser(
    username: String,
    password: String,
    publicKey: String,
    salt: String,
    encryptedPrivateKey: String
): Long = transaction {
    User.selectAll().forEach {
        println(it[User.username])
    }
    if(User.select{User.username eq username}.count() != 0){
        zError("User already exists: $username")
    }
    User.insert {
        it[User.username] = username
        it[User.password] = BCrypt.hashpw(password, ZHTConfig.authSalt)
        it[User.publicKey] = publicKey
        it[User.salt] = salt
        it[User.encryptedPrivateKey] = encryptedPrivateKey
    }
    User.maxValue(User.id) ?: zError("failed to create user")
}

fun authorizeUser(username: String, password: String): Long = transaction {
    val result = User.select {
        User.username eq username
    }.firstOrNull() ?: zError("user not exists: $username")
    val hashedPassword = result[User.password]
    if(!BCrypt.checkpw(password, hashedPassword)){
        zError("Invalid password: $username")
    }
    result[User.id]
}

fun getSaltByUsername(username: String): String = transaction {
    val result = User.select {
        User.username eq username
    }.firstOrNull() ?: zError("No such user")
    result[User.salt]
}

fun getUserIdByAPIToken(token: String): Long? = transaction {
    APIToken.select { APIToken.token eq token }.firstOrNull()?.get(APIToken.userId)
}

fun getUserIdByWorkerToken(token: String): Long? = transaction {
    RegisteredWorker.select { RegisteredWorker.token eq token }.firstOrNull()?.get(RegisteredWorker.userId)
}

fun getUserPublicKey(userId: Long): String = transaction {
    User.select{ User.id eq userId }.firstOrNull()?.get(User.publicKey) ?: zError("user not found")
}

fun getUserInfoByUserId(userId: Long): UserInformationResponse = transaction {
    val result = User.select { User.id eq userId }.firstOrNull() ?: zError("No such user: $userId")
    UserInformationResponse(
        id = result[User.id],
        username = result[User.username],
        publicKey = result[User.publicKey],
        encryptedPrivateKey = result[User.encryptedPrivateKey]
    )
}

fun deleteUserData(userId: Long) = transaction {
    User.deleteWhere { User.id eq userId }
}