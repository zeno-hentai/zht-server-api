package data.http.user

abstract class UserStatusResponse(val authorized: Boolean)

class UserInformationResponse(
    val id: Long,
    val username: String,
    val publicKey: String,
    val encryptedPrivateKey: String
): UserStatusResponse(true)

class UserUnauthorizedResponse(): UserStatusResponse(false)