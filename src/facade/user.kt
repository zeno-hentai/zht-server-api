package facade

import service.getAllItemsOfUser

fun deleteUser(userId: Long) {
    getAllItemsOfUser(userId).forEach { itemId ->
        deleteItem(userId, itemId)
    }
}