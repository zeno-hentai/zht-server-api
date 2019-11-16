package service

import config.GlobalFileManager
import model.FileLink
import model.ItemIndex
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utils.zError
import java.io.InputStream


fun getItemFileData(itemId: Long): InputStream = transaction {
    val fileName = (ItemIndex innerJoin FileLink)
        .select { ItemIndex.id eq itemId }
        .firstOrNull()
        ?.let {
            it[FileLink.name]
        } ?: zError("Item not found: $itemId")
    GlobalFileManager.getFile(fileName)
}