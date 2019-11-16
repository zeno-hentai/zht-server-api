import config.GlobalFileManager
import config.GlobalKVService
import config.ZHTConfig
import config.connectDatabase
import kotlinx.serialization.toUtf8Bytes
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue


class ConfigTest {
//    @Test
//    fun testConfigDebug(){
//        assertEquals("jdbc:sqlite::memory:", ZHTConfig.dbUrl)
//        assertEquals("org.sqlite.JDBC", ZHTConfig.dbDriver)
//    }
//
//    @Test
//    fun databaseConnection(){
//        connectDatabase()
//    }
//
//    @Test
//    fun kvTest(){
//        val key = "test.key"
//        val value = "testValue"
//        assertNull(GlobalKVService[key])
//        GlobalKVService[key] = value
//        assertEquals(value, GlobalKVService[key])
//
//    }
//
//    @Test
//    fun fileTest(){
//        val name = "test.txt"
//        val content = "233332333232323333zzz啊啊啊啊啊".toUtf8Bytes()
//        assertFalse(GlobalFileManager.fileExists(name))
//        GlobalFileManager.addFile(name, content.inputStream())
//        assertTrue(GlobalFileManager.fileExists(name))
//        val result = GlobalFileManager.getFile(name).readAllBytes()
//        assertTrue(content.contentEquals(result))
//        GlobalFileManager.deleteFile(name)
//        assertFalse(GlobalFileManager.fileExists(name))
//    }
}