package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object APIToken: Table("api_token"){
    val id = long("id").autoIncrement().primaryKey()
    val token = varchar("token", 1024)
    val title = varchar("title", 1024)
    val userId = long("user_id").references(User.id, onDelete = ReferenceOption.CASCADE)
}