package config

import service.file.ZHTFileManager
import service.kv.MemoryKeyValueService
import service.kv.PrefixKeyValueService
import service.kv.ZHTKeyValueService

private inline fun <reified T> lazyLoadClassByName(crossinline getName: () -> String): Lazy<T> = lazy {
    val clazz = Class.forName(getName())
    clazz.getDeclaredConstructor().newInstance() as T
}

val GlobalFileManager by lazyLoadClassByName<ZHTFileManager>{ ZHTConfig.fileManagerClass }

private val GlobalKVService by lazyLoadClassByName<ZHTKeyValueService>{ ZHTConfig.kvServiceClass }
val SessionKVService by lazy { PrefixKeyValueService(GlobalKVService, "session") }
val WSKVService: ZHTKeyValueService by lazy { MemoryKeyValueService() }