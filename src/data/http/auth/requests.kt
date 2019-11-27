package data.http.auth

interface MasterKeyRequest {
    val masterKey: String
}

data class RegisterRequest(
    val username: String,
    val password: String,
    val salt: String,
    val publicKey: String,
    val encryptedPrivateKey: String,
    override val masterKey: String
    ): MasterKeyRequest

class LoginRequest (
    val username: String,
    val password: String
    )