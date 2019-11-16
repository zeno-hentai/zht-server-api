package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


object FileLink: Table("file_links") {
    val id = long("id").autoIncrement().primaryKey()
    val name = varchar("name", 1024).uniqueIndex()
    val fileIndex = integer("file_index")
    val itemIndexId = long("item_index_id")
        .references(ItemIndex.id, onDelete = ReferenceOption.CASCADE)
}