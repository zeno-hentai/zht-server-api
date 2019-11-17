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

fun authorizeFile(userId: Long, name: String): Boolean = transaction {
    (User innerJoin ItemIndex innerJoin FileLink).select {
        (User.id eq userId) and (FileLink.name eq name)
    }.count() != 0
}

fun addPackagedFileIndex(
    encryptedMeta: String,
    encryptedKey: String,
    encryptedTags: List<String>,
    previewFile: String,
    files: List<PackagedFileLink>,
    userId: Long
): Long = transaction {
    ItemIndex.insert {
        it[ItemIndex.ownerId] = userId
        it[ItemIndex.encryptedKey] = encryptedKey
        it[ItemIndex.encryptedMeta] = encryptedMeta
        it[ItemIndex.previewFile] = previewFile
    }
    val itemId = ItemIndex.maxValue(ItemIndex.id) ?: zError("failed to add file")

    FileLink.batchInsert(files) { fp ->
        this[FileLink.fileIndex] = fp.index
        this[FileLink.name] = fp.name
        this[FileLink.itemIndexId] = itemId
    }
    ItemTag.batchInsert(encryptedTags) { tag ->
        this[ItemTag.itemId] = itemId
        this[ItemTag.encryptedTag] = tag
    }
    updateLatestUpdateTime(userId)
    itemId
}

fun getFileNamesByItemId(userId: Long, itemId: Long): List<String> = transaction {
    (User innerJoin ItemIndex innerJoin FileLink)
        .slice(FileLink.name)
        .select {
            (User.id eq userId) and (ItemIndex.id eq itemId)
        }
        .orderBy(FileLink.fileIndex, SortOrder.ASC)
        .map{
            it[FileLink.name]
        }
}