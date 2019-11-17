package service

import data.http.api.GenerateAPITokenResponse
import data.http.api.QueryAPITokenResponse
import model.APIToken
import model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.maxValue
import utils.zError

fun addApiToken(userId: Long, title: String, token: String) = transaction {
    APIToken.insert {
        it[APIToken.userId] = userId
        it[APIToken.title] = title
        it[APIToken.token] = token
    }
    val tokenId = APIToken.maxValue(APIToken.id) ?: zError("failed to create item")
    GenerateAPITokenResponse(
        id = tokenId,
        title = title,
        token = token
    )
}

fun queryApiTokensByUserId(userId: Long): List<QueryAPITokenResponse> = transaction {
    APIToken.select {
        APIToken.userId eq userId
    }.orderBy(APIToken.id, SortOrder.DESC)
        .map{
            QueryAPITokenResponse(
                id = it[APIToken.id],
                title = it[APIToken.title]
            )
        }
}

fun deleteApiToken(userId: Long, tokenId: Long): Unit = transaction {
    if((User innerJoin APIToken).select { (User.id eq userId) and (APIToken.id eq tokenId) }.count() == 0){
        zError("unauthorized")
    }
    APIToken.deleteWhere {
        (APIToken.id eq tokenId)
    }
}