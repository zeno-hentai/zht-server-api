package utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.receiveOrNull
import java.io.Closeable


object WorkerNotificationChannels {
    val map = mutableMapOf<Long, Handler>()
    suspend fun send(workerId: Long){
        map[workerId]?.send()
    }
    fun exists(workerId: Long) = map[workerId] != null
    fun getPublicKey(workerId: Long) = map[workerId]?.encryptedPublicKey
    fun open(workerId: Long, encryptedPublicKey: String): Handler{
        if(workerId in map){
            map[workerId]?.close()
        }
        val handler = Handler(workerId, encryptedPublicKey)
        map[workerId] = handler
        return handler
    }

    class Handler(private val workerId: Long, val encryptedPublicKey: String): Closeable {
        private var closed = false
        private val channel = Channel<Unit>()
        suspend fun next(){
            channel.receive()
        }
        suspend fun send(){
            channel.send(Unit)
        }
        override fun close() {
            if(!closed) {
                map.remove(workerId)
                channel.close()
                closed = true
            }
        }
    }
}