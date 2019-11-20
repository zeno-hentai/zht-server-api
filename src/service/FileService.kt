package service

import model.FileLink
import model.ItemIndex
import model.ItemTag
import model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.maxValue
import utils.zError

fun authorizeFile(userId: Long, itemId: Long, name: String): Boolean = transaction {
    (User innerJoin ItemIndex innerJoin FileLink).select {
        (User.id eq userId) and (ItemIndex.id eq itemId) and (FileLink.mappedName eq name)
    }.count() != 0
}

fun getFileNamesByItemId(userId: Long, itemId: Long): Map<String, String> = transaction {
    (User innerJoin ItemIndex innerJoin FileLink)
        .slice(FileLink.encryptedOriginalName, FileLink.mappedName)
        .select {
            (User.id eq userId) and (ItemIndex.id eq itemId)
        }
        .map{
            it[FileLink.encryptedOriginalName] to it[FileLink.mappedName]
        }.toMap()
}

fun addItemIndexRecord(
    userId: Long,
    encryptedMeta: String,
    encryptedKey: String,
    encryptedTags: List<String>
): Long = transaction {
    ItemIndex.insert {
        it[ItemIndex.ownerId] = userId
        it[ItemIndex.encryptedKey] = encryptedKey
        it[ItemIndex.encryptedMeta] = encryptedMeta
    }
    val itemId = ItemIndex.maxValue(ItemIndex.id) ?: zError("failed to add file")
    ItemTag.batchInsert(encryptedTags) { tag ->
        this[ItemTag.itemId] = itemId
        this[ItemTag.encryptedTag] = tag
    }
    itemId
}

fun addFileLinkToItemIndex(
    userId: Long,
    itemId: Long,
    encryptedOriginalName: String,
    mappedFileName: String,
    uploadCallback: () -> Unit
): Unit = transaction {
    if((User innerJoin ItemIndex).select{(User.id eq userId) and (ItemIndex.id eq itemId)}.count() == 0){
        zError("unauthorized")
    }
    FileLink.insert {
        it[FileLink.itemIndexId] = itemId
        it[FileLink.encryptedOriginalName] = encryptedOriginalName
        it[FileLink.mappedName] = mappedFileName
    }
    uploadCallback()
}

fun deleteFileLinkFromItemIndex(
    userId: Long,
    itemId: Long,
    mappedFileName: String,
    deleteCallback: () -> Unit
): Unit = transaction {
    if((User innerJoin ItemIndex).select{
            (User.id eq userId) and
                    (ItemIndex.id eq itemId) and
                    (FileLink.mappedName eq mappedFileName)
        }.count() == 0){
        zError("unauthorized")
    }
    FileLink.deleteWhere { FileLink.mappedName eq mappedFileName }
    deleteCallback()
}