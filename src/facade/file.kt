package facade

import com.google.gson.Gson
import config.GlobalFileManager
import data.http.file.PackagedFileLink
import data.http.item.RequestPackedMetaData
import service.addPackagedFileIndex
import utils.zError
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipInputStream

fun transferFileFromFileManager(name: String, out: OutputStream){
    GlobalFileManager.getFile(name).transferTo(out)
}


private fun ZipInputStream.entries() = sequence {
    var entry = nextEntry
    while(entry != null){
        yield(entry)
        entry = nextEntry
    }
}

fun unpackResourceFile(userId: Long, inputStream: InputStream){
    val zipStream = ZipInputStream(inputStream)
    val gson = Gson()
    val fileMap = mutableMapOf<String, ByteArray>()
    var indexData: RequestPackedMetaData? = null
    zipStream.entries().forEach {
        if(it.name == "index.json"){
            val data = it.extra.toString(Charsets.UTF_8)
            indexData = gson.fromJson(data, RequestPackedMetaData::class.java)
        }else{
            fileMap[it.name] = it.extra
        }
    }
    val meta = indexData ?: zError("missing index.json")
    val fileList = (meta.files ?: zError("missing files")).withIndex().map { (idx, nm) ->
        val name = UUID.randomUUID().toString()
        val data = fileMap[nm] ?: zError("file not exists: $nm")
        GlobalFileManager.addFile(name, data.inputStream())
        PackagedFileLink(idx, name)
    }
    addPackagedFileIndex(
        encryptedKey = meta.encryptedKey ?: zError("missing encryptedKey"),
        encryptedMeta = meta.encryptedMeta ?: zError("missing encryptedMeta"),
        files = fileList,
        userId = userId
    )
}