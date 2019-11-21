package service

import model.ItemIndex
import model.ItemLatestUpdate
import model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

fun queryUpdatedItemsAfter(userId: Long, after: DateTime): List<Long> = transaction {
    (User innerJoin ItemIndex innerJoin ItemLatestUpdate)
        .slice(ItemIndex.id)
        .select { (User.id eq userId) and (ItemLatestUpdate.latestUpdateTime greaterEq  after) }
        .map {
            it[ItemIndex.id]
        }
}

fun Transaction.updateLatestUpdateTime(itemId: Long) {
    val dateTime = DateTime.now()!!
    if(ItemLatestUpdate.select { ItemLatestUpdate.itemId eq itemId }.count() == 0){
        ItemLatestUpdate.insert {
            it[ItemLatestUpdate.itemId] = itemId
        }
    }
    ItemLatestUpdate.update({ ItemLatestUpdate.itemId eq itemId }) {
        it[ItemLatestUpdate.latestUpdateTime] = dateTime
    }
}