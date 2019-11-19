package service.kv

class PrefixKeyValueService(val kvService: ZHTKeyValueService, val prefix: String): ZHTKeyValueService {
    override fun get(key: String): String? {
        return kvService["$prefix:$key"]
    }

    override fun set(key: String, value: String) {
        kvService["$prefix:$key"] = value
    }

    override fun delete(key: String) {
        kvService.delete("$prefix:$key")
    }

}