package service

import data.http.item.ItemIndexData
import data.http.item.ItemIndexPaging
import data.http.item.ItemTagData
import model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import utils.zError

fun getItemData(userId: Long, itemId: Long) = transaction {
    val result = (User innerJoin ItemIndex).select{
        (User.id eq userId) and (ItemIndex.id eq itemId)
    }.firstOrNull() ?: zError("can not find item: $itemId")
    val tags = ItemTag.select {
        ItemTag.itemId eq itemId
    }.map {
        ItemTagData(
            id = it[ItemTag.id],
            encryptedTag = it[ItemTag.encryptedTag]
        )
    }
    ItemIndexData(
        id = result[ItemIndex.id],
        encryptedMeta = result[ItemIndex.encryptedMeta],
        encryptedKey = result[ItemIndex.encryptedKey],
        encryptedTags = tags
    )
}

fun getItemPagingOfUser(userId: Long): ItemIndexPaging = transaction {
    val total = (User innerJoin ItemIndex).select { User.id eq userId }.count()
    ItemIndexPaging(total)
}

fun getAllItemsOfUser(userId: Long): List<Long> = transaction{
    (User innerJoin ItemIndex)
        .slice(ItemIndex.id)
        .select { User.id eq userId }
        .map{
            it[ItemIndex.id]
        }
}

fun queryItemsOfUser(userId: Long, offset: Int, limit: Int): List<ItemIndexData> = transaction {
    val tagMap = (User innerJoin ItemIndex innerJoin ItemTag)
        .slice(ItemTag.id, ItemTag.encryptedTag, ItemIndex.id)
        .select { User.id eq userId }
        .groupBy { it[ItemIndex.id] }
        .map { (itemId, resultList) ->
            itemId to resultList.map {
                ItemTagData(
                    id = it[ItemTag.id],
                    encryptedTag = it[ItemTag.encryptedTag]
                )
            }
        }.toMap()
    (User innerJoin ItemIndex)
        .slice(ItemIndex.id, ItemIndex.encryptedKey, ItemIndex.encryptedMeta)
        .select { User.id eq userId }
        .orderBy(ItemIndex.id, SortOrder.ASC)
        .limit(limit, offset)
        .map {
            val id = it[ItemIndex.id]
            ItemIndexData(
                id = id,
                encryptedMeta = it[ItemIndex.encryptedMeta],
                encryptedKey = it[ItemIndex.encryptedKey],
                encryptedTags = tagMap[id] ?: emptyList()
            )
        }
}

fun queryMappedFileListByItemId(userId: Long, itemId: Long): List<String> = transaction {
    (User innerJoin ItemIndex innerJoin FileLink)
        .slice(FileLink.mappedName)
        .select { (User.id eq userId) and (ItemIndex.id eq itemId) }
        .map{
            it[FileLink.mappedName]
        }
}

fun deleteItemIndex(userId: Long, itemId: Long): Unit = transaction {
    if(ItemIndex.select {
            (ItemIndex.ownerId eq userId) and
                    (ItemIndex.id eq itemId)
        }.count() == 0){
        zError("userId and itemId not match")
    }
    FileLink.deleteWhere { FileLink.itemIndexId eq itemId }
    ItemTag.deleteWhere { ItemTag.itemId eq itemId }
    ItemLatestUpdate.deleteWhere { ItemLatestUpdate.itemId eq itemId }
    ItemIndex.deleteWhere { ItemIndex.id eq itemId }
    ItemDeleteRecord.insert {
        it[ItemDeleteRecord.itemId] = itemId
        it[ItemDeleteRecord.userId] = userId
        it[ItemDeleteRecord.deletedAt] = DateTime.now()
    }
}

fun deletedItemsAfter(userId: Long, after: DateTime): List<Long> = transaction {
    ItemDeleteRecord
        .slice(ItemDeleteRecord.itemId)
        .select {
            (ItemDeleteRecord.userId eq userId) and (ItemDeleteRecord.deletedAt greaterEq after)
        }.map{ it[ItemDeleteRecord.itemId] }
}