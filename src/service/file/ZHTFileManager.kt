package service.file

import java.io.InputStream

interface ZHTFileManager {
    fun addFile(name: String, data: InputStream)
    fun fileExists(name: String): Boolean
    fun getFile(name: String): InputStream
    fun deleteFile(name: String)
}