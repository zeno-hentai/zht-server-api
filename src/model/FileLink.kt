package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


object FileLink: Table("file_links") {
    val id = long("id").autoIncrement().primaryKey()
    val encryptedOriginalName = varchar("encrypted_original_name", 1024).uniqueIndex()
    val mappedName = varchar("mapped_name", 1024).uniqueIndex()
    val itemIndexId = long("item_index_id")
        .references(ItemIndex.id, onDelete = ReferenceOption.CASCADE)
}