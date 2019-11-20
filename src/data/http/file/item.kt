package data.http.file

data class PackagedFileLink(val encryptedOriginalName: String, val mappedName: String)

data class UploadResponse(val id: Long)