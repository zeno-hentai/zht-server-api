
import config.ZHTConfig
import config.connectDatabase
import config.createDatabaseTables
import controller.routingRoot
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.websocket.webSocket
import org.slf4j.event.Level
import utils.ZhtApiException
import utils.api.ZHTSession
import utils.api.ZhtApiErrorResponse
import java.text.DateFormat
import java.time.Duration

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
        level = ZHTConfig.logLevel
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
            call.respond(ZhtApiErrorResponse(it.message))
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

