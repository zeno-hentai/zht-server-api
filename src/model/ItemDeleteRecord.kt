package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ItemDeleteRecord: Table("item_delete_record") {
    val itemId = long("itemId").primaryKey()
    val userId = long("user_id").references(User.id, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
    val deletedAt = datetime("deleted_at")
}