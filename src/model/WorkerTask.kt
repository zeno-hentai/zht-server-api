package model

import data.http.api.WorkerTaskStatus
import model.APIToken.autoIncrement
import model.APIToken.primaryKey
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object WorkerTask: Table("worker_task") {
    val id = long("id").autoIncrement().primaryKey()
    val encryptedURLToWorker = text("encrypted_url_to_worker")
    val encryptedURLToUser = text("encrypted_url_to_user")
    val workerId = long("worker_id").references(RegisteredWorker.id, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
    val status = enumeration("status", WorkerTaskStatus::class)
}