package data.http.item

data class RequestPackedMetaData(
    val encryptedMeta: String,
    val encryptedKey: String,
    val files: List<String>
)