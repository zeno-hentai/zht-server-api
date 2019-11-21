package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

object ItemLatestUpdate: Table("item_latest_updates") {
    val itemId = long("item_id").primaryKey().references(ItemIndex.id, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
    val latestUpdateTime = datetime("latest_update_time").default(DateTime(0))
}