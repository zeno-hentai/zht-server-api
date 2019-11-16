package config

import model.APIToken
import model.FileLink
import model.ItemIndex
import model.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun connectDatabase() {
    Database.connect(ZHTConfig.dbUrl, driver = ZHTConfig.dbDriver)
}

fun createDatabaseTables() = transaction {
    SchemaUtils.create(User, FileLink, ItemIndex, APIToken)
    commit()
}