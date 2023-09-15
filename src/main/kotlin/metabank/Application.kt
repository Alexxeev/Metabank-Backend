package metabank

import metabank.plugins.configureCors
import metabank.plugins.configureExceptionHandlers
import metabank.plugins.configureRouting
import metabank.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureCors()
    configureSerialization()
    configureExceptionHandlers()
    configureRouting()
}
