package service.file

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.natpryce.konfig.stringType
import config.ZHTConfig
import java.io.InputStream

object AwsS3FileManager: ZHTFileManager {
    private val client = AmazonS3ClientBuilder
        .standard()
        .withRegion(Regions.DEFAULT_REGION)
        .build()
    private val bucket = ZHTConfig.getProperty("service.file.s3.bucket", stringType)

    override fun addFile(name: String, data: InputStream) {
        val meta = ObjectMetadata()
        client.putObject(bucket, name, data, meta)
    }

    override fun fileExists(name: String): Boolean {
        return client.doesObjectExist(bucket, name)
    }

    override fun getFile(name: String): InputStream {
        val obj = client.getObject(bucket, name)
        return obj.objectContent
    }

    override fun deleteFile(name: String) {
        client.deleteObject(bucket, name)
    }
}