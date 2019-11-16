package controller.http

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.route
import utils.zError

fun Route.apiRouting(){
    route("token") {
//        post("create") {
//            call.respond(createAPIToken())
//        }

        delete("delete/{id}") {
            val id = call.parameters["id"]?.toInt() ?: zError("Missing ID")

        }
    }
}