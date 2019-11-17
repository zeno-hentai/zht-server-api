package controller

import controller.http.*
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.route

fun Routing.routingRoot() {
    route("api") {
        route("user", Route::userRouting)
        route("item", Route::itemRouting)
        route("auth", Route::authRouting)
        route("file", Route::fileRouting)
        route("api", Route::apiRouting)
    }
}