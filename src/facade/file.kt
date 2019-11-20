package facade

import config.GlobalFileManager
import data.http.item.CreateItemRequest
import service.addFileLinkToItemIndex
import service.addItemIndexRecord
import service.deleteFileLinkFromItemIndex
import java.io.InputStream
import java.io.OutputStream
import java.util.*

fun transferFileFromFileManager(name: String, out: OutputStream){
    GlobalFileManager.getFile(name).transferTo(out)
}

fun createItemIndex(userId: Long, request: CreateItemRequest): Long =
    addItemIndexRecord(
        userId = userId,
        encryptedMeta = request.encryptedMeta,
        encryptedKey = request.encryptedKey,
        encryptedTags = request.encryptedTags
    )

fun addFileToItem(userId: Long, itemId: Long, encryptedFileName: String, dataStream: InputStream): String {
    val mappedFileName = UUID.randomUUID().toString()
    addFileLinkToItemIndex(userId, itemId, encryptedFileName, mappedFileName) {
        GlobalFileManager.addFile(mappedFileName, dataStream)
    }
    return mappedFileName
}

fun deleteFileFromItem(userId: Long, itemId: Long, mappedFileName: String) {
    deleteFileLinkFromItemIndex(userId, itemId, mappedFileName) {
        GlobalFileManager.deleteFile(mappedFileName)
    }
}
