package service

import model.UserLatestUpdate
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

fun queryLatestUpdateTime(userId: Long) = transaction {
    UserLatestUpdate
        .select { UserLatestUpdate.userId eq userId }
        .firstOrNull()
        ?.let {
            it[UserLatestUpdate.latestUpdateTime]
        } ?: DateTime(0)
}

fun Transaction.updateLatestUpdateTime(userId: Long) {
    val dateTime = DateTime.now()!!
    if(UserLatestUpdate.select { UserLatestUpdate.userId eq userId }.count() == 0){
        UserLatestUpdate.insert {
            it[UserLatestUpdate.userId] = userId
        }
    }
    UserLatestUpdate.update({ UserLatestUpdate.userId eq userId }) {
        it[UserLatestUpdate.latestUpdateTime] = dateTime
    }
}