package data.http.user

data class UserInformationResponse(
    val id: Long,
    val username: String,
    val publicKey: String,
    val encryptedPrivateKey: String
)