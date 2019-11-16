package model

import org.jetbrains.exposed.sql.Table

object User: Table("users") {
    val id = long("id").autoIncrement().primaryKey()
    val username = varchar("username",  512).uniqueIndex()
    val password = varchar("password",  1024)
    val publicKey = varchar("public_key",  2048)
    val encryptedPrivateKey = varchar("encrypted_private_key",  2048)
}