package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ItemIndex: Table("item_indices") {
    val id = long("id").autoIncrement().primaryKey()
    val encryptedMeta = text("encrypted_meta")
    val encryptedKey = text("encrypted_key")
    val ownerId = long("owner_id").references(User.id, onDelete = ReferenceOption.CASCADE)
}