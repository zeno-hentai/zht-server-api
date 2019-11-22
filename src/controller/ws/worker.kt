package controller.ws

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.WebSocketServerSession
import service.getWorkerIdByToken
import utils.WorkerNotificationChannels
import utils.zError

suspend fun WebSocketServerSession.wsWorker() {
    val frame = incoming.receive()
    if(frame is Frame.Text){
        val token = frame.readText()
        val workerId = getWorkerIdByToken(token) ?: zError("Invalid token")
        val ch = WorkerNotificationChannels.open(workerId)
        while(true) {
            ch.next()
            outgoing.send(Frame.Text("new"))
        }
    }else{
        close()
    }
}