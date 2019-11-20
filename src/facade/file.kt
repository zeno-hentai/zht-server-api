package facade

import com.google.gson.Gson
import config.GlobalFileManager
import data.http.file.PackagedFileLink
import data.http.item.CreateItemRequest
import data.http.item.RequestPackedMetaData
import service.addFileLinkToItemIndex
import service.addItemIndexRecord
import service.addPackagedFileIndex
import service.deleteFileLinkFromItemIndex
import utils.zError
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

fun transferFileFromFileManager(name: String, out: OutputStream){
    GlobalFileManager.getFile(name).transferTo(out)
}

fun createItemIndex(userId: Long, request: CreateItemRequest): Long =
    addItemIndexRecord(
        userId = userId,
        encryptedMeta = request.encryptedMeta,
        encryptedKey = request.encryptedKey,
        encryptedTags = request.encryptedTags
    )

fun addFileToItem(userId: Long, itemId: Long, encryptedFileName: String, dataStream: InputStream): String {
    val mappedFileName = UUID.randomUUID().toString()
    addFileLinkToItemIndex(userId, itemId, encryptedFileName, mappedFileName) {
        GlobalFileManager.addFile(mappedFileName, dataStream)
    }
    return mappedFileName
}

fun deleteFileFromItem(userId: Long, itemId: Long, mappedFileName: String) {
    deleteFileLinkFromItemIndex(userId, itemId, mappedFileName) {
        GlobalFileManager.deleteFile(mappedFileName)
    }
}

//private fun ZipInputStream.entries() = sequence {
//    var entry = nextEntry
//    while(entry != null){
//        if(!entry.isDirectory){
//            yield(entry.name to readNBytes(entry.size.toInt()))
//        }
//        entry = nextEntry
//    }
//    close()
//}
//
//fun unpackResourceFile(userId: Long, inputStream: InputStream): Long{
//    val zipStream = ZipInputStream(inputStream)
//    val gson = Gson()
//    val fileMap = mutableMapOf<String, ByteArray>()
//    var indexData: RequestPackedMetaData? = null
//    zipStream.entries().forEach { (name, data) ->
//        if(name == "index.json"){
//            indexData = gson.fromJson(data.toString(Charsets.UTF_8), RequestPackedMetaData::class.java)
//        }else{
//            fileMap[name] = data
//        }
//    }
//    val meta = indexData ?: zError("missing index.json")
//    val fileNameMapping = meta.files.map {
//        it to UUID.randomUUID().toString()
//    }.toMap()
//    val fileList = meta.files.withIndex().map { (idx, nm) ->
//        val resName = "resource/$nm"
//        val name = fileNameMapping[nm] ?: zError("unknown file: $nm")
//        val data = fileMap[resName] ?: zError("file not exists: $resName")
//        GlobalFileManager.addFile(name, data.inputStream())
//        PackagedFileLink(nm, name)
//    }
//    return addPackagedFileIndex(
//        encryptedKey = meta.encryptedKey,
//        encryptedMeta = meta.encryptedMeta,
//        encryptedTags = meta.encryptedTags,
//        files = fileList,
//        userId = userId
//    )
//}