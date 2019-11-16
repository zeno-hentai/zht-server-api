package data

data class ItemIndexData(var id: Long, var encryptedMeta: String, var encryptedKey: String) {
    constructor(): this(-1, "", "")
}