package facade

import config.GlobalFileManager
import service.deleteItemIndex
import service.queryMappedFileListByItemId

fun deleteItem(userId: Long, itemId: Long) {
    val fileNames = queryMappedFileListByItemId(userId, itemId)
    deleteItemIndex(userId, itemId)
    fileNames.forEach {
        GlobalFileManager.deleteFile(it)
    }
}