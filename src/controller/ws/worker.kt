package controller.ws

import com.google.gson.Gson
import data.http.api.WorkerConnectionRequest
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.WebSocketServerSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import service.cancelAllTasks
import service.getWorkerIdByToken
import utils.WorkerNotificationChannels
import utils.zError

private suspend fun WebSocketServerSession.nextText(): String {
    val frame = incoming.receive()
    if(frame is Frame.Text){
        return frame.readText()
    }else{
        zError("Invalid WS Message")
    }
}

@ExperimentalCoroutinesApi
private suspend fun WebSocketServerSession.startWorkerNotification(
    token: String,
    encryptedPublicKey: String
) {
    val workerId = getWorkerIdByToken(token) ?: zError("Invalid token")
    cancelAllTasks(workerId)
    outgoing.send(Frame.Text("connected"))
    WorkerNotificationChannels
        .open(workerId, encryptedPublicKey)
        .use { ch ->
            outgoing.invokeOnClose {
                ch.close()
            }
            while(true){
                ch.next()
                outgoing.send(Frame.Text("new"))
            }
        }
}

suspend fun WebSocketServerSession.wsWorker() {
    val frame = incoming.receive()
    if(frame is Frame.Text){
        try{
            val (token, encryptedPublicKey) = Gson().fromJson(frame.readText(), WorkerConnectionRequest::class.java)
            startWorkerNotification(token, encryptedPublicKey)
        } finally {
            close()
        }
    }else{
        close()
    }
}