package model

import org.jetbrains.exposed.sql.Table

object User: Table("users") {
    val id = long("id").autoIncrement().primaryKey()
    val username = varchar("username",  512).uniqueIndex()
    val password = varchar("password",  1024)
    val publicKey = text("public_key")
    val encryptedPrivateKey = text("encrypted_private_key")
}