package facade

import service.deleteUserData
import service.getAllItemsOfUser

fun deleteUser(userId: Long) {
    getAllItemsOfUser(userId).forEach { itemId ->
        deleteItem(userId, itemId)
    }
    deleteUserData(userId)
}