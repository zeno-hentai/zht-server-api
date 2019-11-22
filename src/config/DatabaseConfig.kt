package config

import model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import utils.zError

fun connectDatabase() {
    val username = ZHTConfig.dbUsername
    val password = ZHTConfig.dbPassword
    if(username != null && password != null){
        Database.connect(
            ZHTConfig.dbUrl,
            driver = ZHTConfig.dbDriver,
            user = username,
            password = password)
    }else if(username == null && password == null) {
        Database.connect(ZHTConfig.dbUrl, driver = ZHTConfig.dbDriver)
    }else {
        zError("db.user and db.password not all null")
    }
}

fun createDatabaseTables() = transaction {
    SchemaUtils.create(
        User, FileLink, ItemIndex, APIToken,
        ItemTag, ItemLatestUpdate, ItemDeleteRecord,
        RegisteredWorker, WorkerTask)
    commit()
}