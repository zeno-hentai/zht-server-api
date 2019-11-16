package service.kv

interface ZHTKeyValueService {
    operator fun get(key: String): String?
    operator fun set(key: String, value: String)
    fun delete(key: String)
}