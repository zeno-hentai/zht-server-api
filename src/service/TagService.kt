package service

import data.http.item.AddItemTagResponse
import model.ItemIndex
import model.ItemTag
import model.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utils.maxValue
import utils.zError

fun addTagToItem(userId: Long, itemId: Long, encryptedTag: String) = transaction {
    if(
        (User innerJoin ItemIndex)
            .select { (User.id eq userId) and (ItemIndex.id eq itemId) }
            .count() == 0) {
        zError("Unauthorized")
    }
    ItemTag.insert {
        it[ItemTag.itemId] = itemId
        it[ItemTag.encryptedTag] = encryptedTag
    }
    val id = ItemTag.maxValue(ItemTag.id) ?: zError("failed to add tag")
    updateLatestUpdateTime(itemId)
    AddItemTagResponse(id, itemId, encryptedTag)
}

fun deleteTagFromItem(userId: Long, tagId: Long) = transaction {
    val itemId = (User innerJoin ItemIndex innerJoin ItemTag)
        .slice(ItemIndex.id)
        .select { (User.id eq userId) and (ItemTag.id eq tagId) }
        .firstOrNull()?.get(ItemIndex.id)
    if(itemId == null) {
        zError("Unauthorized")
    }else{
        ItemTag.deleteWhere { ItemTag.id eq tagId }
        updateLatestUpdateTime(itemId)
    }
}