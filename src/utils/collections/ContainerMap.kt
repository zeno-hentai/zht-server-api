package utils.collections

class ContainerMap<K, V, C: MutableCollection<V>>(
    private val map: MutableMap<K, C>,
    private val classBuilder: () -> C
): Map<K, C> by map {
    fun put(key: K, value: V): Iterator<V> {
        val target = map.getOrPut(key){ classBuilder() }
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