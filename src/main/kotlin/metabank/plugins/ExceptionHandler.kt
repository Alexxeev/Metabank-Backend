package metabank.plugins

import metabank.exception.NoSuchDatabaseException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureExceptionHandlers() {
    install(StatusPages) {
        exception<NoSuchDatabaseException> { call, exception ->
            call.respond(
                HttpStatusCode.BadRequest,
                "No such database with specified url: ${exception.url}")
        }
        exception<MissingRequestParameterException> { call, exception ->
            call.respond(
                HttpStatusCode.BadRequest,
                "Missing required parameter: ${exception.parameterName}"
            )
        }
        exception<ParameterConversionException> { call, exception ->
            call.respond(
                HttpStatusCode.BadRequest,
                "Parameter ${exception.parameterName} is incorrect."
            )
        }
    }
}