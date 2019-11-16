package service

import data.http.file.PackagedFileLink
import model.FileLink
import model.ItemIndex
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
    files: List<PackagedFileLink>,
    userId: Long
): Long = transaction {
    ItemIndex.insert {
        it[ItemIndex.ownerId] = userId
        it[ItemIndex.encryptedKey] = encryptedKey
        it[ItemIndex.encryptedMeta] = encryptedMeta
    }
    val itemId = ItemIndex.maxValue(ItemIndex.id) ?: zError("failed to add file")
    files.forEach {fp ->
        FileLink.insert {
            it[FileLink.fileIndex] = fp.index
            it[FileLink.name] = fp.name
            it[FileLink.itemIndexId] = itemId
        }
    }
    updateLatestUpdateTime(userId)
    itemId
}