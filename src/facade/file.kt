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
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

fun transferFileFromFileManager(name: String, out: OutputStream){
    GlobalFileManager.getFile(name).transferTo(out)
}


private fun ZipInputStream.entries() = sequence {
    var entry = nextEntry
    while(entry != null){
        yield(entry.name to readNBytes(entry.size.toInt()))
        entry = nextEntry
    }
    close()
}

fun unpackResourceFile(userId: Long, inputStream: InputStream): Long{
    val zipStream = ZipInputStream(inputStream)
    val gson = Gson()
    val fileMap = mutableMapOf<String, ByteArray>()
    var indexData: RequestPackedMetaData? = null
    zipStream.entries().forEach { (name, data) ->
        if(name == "index.json"){
            indexData = gson.fromJson(data.toString(Charsets.UTF_8), RequestPackedMetaData::class.java)
        }else{
            fileMap[name] = data
        }
    }
    val meta = indexData ?: zError("missing index.json")
    val fileNameMapping = meta.files.map {
        it to UUID.randomUUID().toString()
    }.toMap()
    val previewFile = fileNameMapping[meta.previewFile] ?: zError("missing previewFile")
    val fileList = meta.files.withIndex().map { (idx, nm) ->
        val name = fileNameMapping[nm] ?: zError("?")
        val data = fileMap[nm] ?: zError("file not exists: $nm")
        GlobalFileManager.addFile(name, data.inputStream())
        PackagedFileLink(idx, name)
    }
    return addPackagedFileIndex(
        encryptedKey = meta.encryptedKey,
        encryptedMeta = meta.encryptedMeta,
        encryptedTags = meta.encryptedTags,
        previewFile = previewFile,
        files = fileList,
        userId = userId
    )
}