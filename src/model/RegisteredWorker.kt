package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object RegisteredWorker: Table("registered_worker") {
    val id = long("id").autoIncrement().primaryKey()
    val token = varchar("token", 256)
    val title = varchar("title", 1024)
    val userId = long("user_id").references(User.id, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
}