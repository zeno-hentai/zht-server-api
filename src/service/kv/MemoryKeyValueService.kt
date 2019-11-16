package service.kv

class MemoryKeyValueService: ZHTKeyValueService {
    private val store = HashMap<String, String>()

    override fun get(key: String): String? = store[key]

    override fun set(key: String, value: String) {
        store[key] = value
    }

    override fun delete(key: String) {
        store.remove(key)
    }
}