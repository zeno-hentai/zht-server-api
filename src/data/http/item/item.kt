package data.http.item

data class RequestPackedMetaData(
    val encryptedMeta: String,
    val encryptedKey: String,
    val encryptedTags: List<String>,
    val files: List<String>
)

data class CreateItemRequest(
    val encryptedMeta: String,
    val encryptedKey: String,
    val encryptedTags: List<String>
)

data class UpdateEncryptedMetaRequest (
    val encryptedMeta: String,
    val itemId: Long
)

data class ItemIndexPaging(
    val total: Int
)

data class ItemTagData(
    val id: Long,
    val encryptedTag: String
)

data class ItemIndexData(
    val id: Long,
    val encryptedMeta: String,
    val encryptedKey: String,
    val encryptedTags: List<ItemTagData>
)