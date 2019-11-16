package facade

import config.GlobalFileManager
import service.deleteItemIndex
import service.queryFileListByItemId

fun deleteItem(userId: Long, itemId: Long) {
    val fileNames = queryFileListByItemId(userId, itemId)
    deleteItemIndex(userId, itemId)
    fileNames.forEach {
        GlobalFileManager.deleteFile(it)
    }
}