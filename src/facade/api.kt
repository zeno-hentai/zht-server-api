package facade

import data.http.api.GenerateAPITokenResponse
import service.addApiToken
import java.util.*

fun createAPIToken(userId: Long, title: String): GenerateAPITokenResponse {
    val token = UUID.randomUUID().toString()
    return addApiToken(userId, title, token)
}