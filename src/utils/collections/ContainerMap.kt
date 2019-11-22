package utils.collections

class ContainerMap<K, V, C: MutableCollection<V>>(
    private val map: MutableMap<K, C>,
    private val collectionClass: Class<C>
): Map<K, C> by map {
    private fun createCollection(): C {
        val collection = collectionClass
            .declaredConstructors
            .first { it.parameters.isEmpty() }
            .newInstance()
        return collectionClass.cast(collection)
    }
    fun put(key: K, value: V): Iterator<V> {
        val target = map.getOrPut(key){ createCollection() }
        target.add(value)
        return target.iterator()
    }
    fun remove(key: K, value: V) {
        map[key]?.remove(value)
        if(map[key].isNullOrEmpty()){
            map.remove(key)
        }
    }
}