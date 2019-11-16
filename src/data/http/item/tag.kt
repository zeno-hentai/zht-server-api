package data.http.item

data class AddItemTagRequest(
    val itemId: Long,
    val encryptedTag: String
)

data class AddItemTagResponse(
    val id: Long,
    val itemId: Long,
    val encryptedTag: String
)