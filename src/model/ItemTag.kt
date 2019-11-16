package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ItemTag: Table("item_tags") {
    val id = long("id").autoIncrement().primaryKey()
    val itemId = long("item_id").references(ItemIndex.id, ReferenceOption.CASCADE)
    val encryptedTag = varchar("encrypted_tag", 1024)
}