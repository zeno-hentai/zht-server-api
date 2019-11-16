package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

object UserLatestUpdate: Table("user_latest_updates") {
    val userId = long("user_id").primaryKey().references(User.id, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
    val latestUpdateTime = datetime("latest_update_time").default(DateTime(0))
}