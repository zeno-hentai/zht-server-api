package service.file

import java.io.InputStream

class MemoryFileManager: ZHTFileManager {
    private val storage = HashMap<String, ByteArray>()

    override fun addFile(name: String, data: InputStream) {
        storage[name] = data.readAllBytes()!!
    }

    override fun fileExists(name: String): Boolean {
        return name in storage
    }

    override fun getFile(name: String): InputStream {
        return storage[name]!!.inputStream()
    }

    override fun deleteFile(name: String) {
        storage.remove(name)
    }
}