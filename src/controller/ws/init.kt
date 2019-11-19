package controller.ws

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Route
import io.ktor.websocket.webSocket
import utils.api.authorizedUserId

fun Route.initializeWS(){
    webSocket("bus") {
        send(Frame.Text("Hi from server"))
        call.authorizedUserId
        while (true) {
            val frame = incoming.receive()
            if (frame is Frame.Text) {
                send(Frame.Text("Client said: " + frame.readText()))
            }
        }
    }
}