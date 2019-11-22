package utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.receiveOrNull
import java.io.Closeable


object WorkerNotificationChannels {
    val map = mutableMapOf<Long, Handler>()
    suspend fun send(workerId: Long){
        map[workerId]?.send()
    }
    fun open(workerId: Long): Handler{
        if(workerId in map){
            map[workerId]?.close()
        }
        val handler = Handler(workerId)
        map[workerId] = handler
        return handler
    }

    class Handler(private val workerId: Long): Closeable {
        private val channel = Channel<Unit>()
        suspend fun next(){
            channel.receive()
        }
        suspend fun send(){
            channel.send(Unit)
        }
        override fun close() {
            println("CLOSED???????")
            map[workerId]!!.close()
            map.remove(workerId)
        }
    }
}