package utils

import java.io.Closeable
import java.util.*

class WSBusQueue<T> {
    private val map: MutableMap<Long, MutableMap<String, MutableSet<Handler<T>>>> = mutableMapOf()
    class Handler<T> (
        private val userId: Long,
        private val roomName: String,
        private val parent: WSBusQueue<T>
    ): Closeable {
        private var closed = false
        private val room = parent.map.getOrPut(userId) {
            mutableMapOf()
        }.getOrPut(roomName) {
            mutableSetOf()
        }
        init {
            room.add(this)
        }
        private val queue: Queue<T> = LinkedList()
        fun put(obj: T){
            if(closed) error("Queue has been closed")
            room.forEach {
                if(it !== this){
                    it.queue.add(obj)
                }
            }
        }
        fun forEach(cb: (T) -> Unit) {
            while(queue.isNotEmpty()){
                cb(queue.poll()!!)
            }
        }
        @Synchronized
        override fun close(){
            closed = true
            room.remove(this)
            if(room.isEmpty()){
                parent.map[userId]?.remove(roomName)
            }
            if(parent.map[userId].isNullOrEmpty()){
                parent.map.remove(userId)
            }
        }
    }
    fun open(userId: Long, roomName: String): Handler<T> {
        return Handler(userId, roomName, this)
    }
}