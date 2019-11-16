
import config.ZHTConfig
import config.connectDatabase
import config.createDatabaseTables
import controller.routingRoot
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import io.ktor.auth.*
import io.ktor.gson.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import org.slf4j.event.Level
import utils.api.ZHTSession
import utils.ZhtApiException
import java.text.DateFormat

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DataConversion)

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(Authentication) {

    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
        }
    }

    install(Sessions) {
        cookie<ZHTSession>("SESSION")
    }

    install(StatusPages) {
        exception <ZhtApiException> {
            call.respond(mapOf(
                "error" to it.message
            ))
            log.trace(it.message, it)
        }
    }

    connectDatabase()
    if(ZHTConfig.dbCreateTables){
        createDatabaseTables()
    }
    log.info("Debug Message: ${ZHTConfig.debugTestProperty}")

    routing {

        webSocket("/myws/echo") {
            send(Frame.Text("Hi from server"))
            while (true) {
                val frame = incoming.receive()
                if (frame is Frame.Text) {
                    send(Frame.Text("Client said: " + frame.readText()))
                }
            }
        }

        routingRoot()

        get("/") {
            call.respondText("SUCCESS", contentType = ContentType.Text.Plain)
        }
    }
}

