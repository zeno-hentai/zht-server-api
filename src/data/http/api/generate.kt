package data.http.api

data class GenerateAPITokenRequest(
    val title: String
)

data class GenerateAPITokenResponse(
    val id: Long,
    val title: String,
    val token: String
)