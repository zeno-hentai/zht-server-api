package service.file

import com.natpryce.konfig.stringType
import config.ZHTConfig
import java.io.File
import java.io.InputStream

class DiskFileManager: ZHTFileManager {
    private val root = File(ZHTConfig.getProperty("service.file.disk.root", stringType))
    private fun resolve(f: String): File {
        return root.resolve(f)
    }

    override fun addFile(name: String, data: InputStream) {
        data.transferTo(resolve(name).outputStream())
    }

    override fun fileExists(name: String): Boolean {
        return resolve(name).exists()
    }

    override fun getFile(name: String): InputStream {
        return resolve(name).inputStream()
    }

    override fun deleteFile(name: String) {
        resolve(name).delete()
    }

}