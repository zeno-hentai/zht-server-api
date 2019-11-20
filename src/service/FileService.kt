package service

import data.http.file.PackagedFileLink
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

fun addPackagedFileIndex(
    encryptedMeta: String,
    encryptedKey: String,
    encryptedTags: List<String>,
    files: List<PackagedFileLink>,
    userId: Long
): Long = transaction {
    ItemIndex.insert {
        it[ItemIndex.ownerId] = userId
        it[ItemIndex.encryptedKey] = encryptedKey
        it[ItemIndex.encryptedMeta] = encryptedMeta
    }
    val itemId = ItemIndex.maxValue(ItemIndex.id) ?: zError("failed to add file")

    FileLink.batchInsert(files) { fp ->
        this[FileLink.encryptedOriginalName] = fp.encryptedOriginalName
        this[FileLink.mappedName] = fp.mappedName
        this[FileLink.itemIndexId] = itemId
    }
    ItemTag.batchInsert(encryptedTags) { tag ->
        this[ItemTag.itemId] = itemId
        this[ItemTag.encryptedTag] = tag
    }
    updateLatestUpdateTime(userId)
    itemId
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