package config

import service.file.ZHTFileManager
import service.kv.ZHTKeyValueService

private inline fun <reified T> lazyLoadClassByName(crossinline getName: () -> String): Lazy<T> = lazy {
    val clazz = Class.forName(getName())
    clazz.getDeclaredConstructor().newInstance() as T
}

val GlobalFileManager by lazyLoadClassByName<ZHTFileManager>{ ZHTConfig.fileManagerClass }
val GlobalKVService by lazyLoadClassByName<ZHTKeyValueService>{ ZHTConfig.kvServiceClass }