package metabank.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import metabank.factory.MetadataDatabase
import metabank.model.DatabaseConnectionModel
import metabank.model.PageQueryModel
import metabank.repository.ColumnRepositoryImpl
import metabank.repository.DatabaseRepositoryImpl
import metabank.repository.MetadataRepository
import metabank.repository.PageQueryRepositoryImpl
import metabank.repository.TableRepositoryImpl
import metabank.service.MetadataService

private const val TEN_MEGABYTES = 10 * 10 * 1024

fun Application.configureRouting() {
    if (Files.notExists(Path.of("db"))) {
        Files.createDirectory(Path.of("db"))
    }
    val databaseRepository = DatabaseRepositoryImpl(MetadataDatabase.instance)
    val tableRepository = TableRepositoryImpl(MetadataDatabase.instance)
    val columnRepository = ColumnRepositoryImpl(MetadataDatabase.instance)
    val pageQueryRepository = PageQueryRepositoryImpl(MetadataDatabase.instance)
    val metadataService = MetadataService(
        MetadataRepository(),
        databaseRepository,
        tableRepository,
        columnRepository
                                         )
    routing {
        get("/databases") {
            val databases = databaseRepository.findAll()
            call.respond(HttpStatusCode.OK, databases)
        }

        post("/databases") {
            val connectionModel = call.receive<DatabaseConnectionModel>()
            if (metadataService.fetchSchema(connectionModel))
                call.respond(HttpStatusCode.Created, "Database successfully created")
            else
                call.respond(HttpStatusCode.OK, "Schema is already fetched")
        }

        get("/databases/{id}") {
            val id = requireNotNull(call.parameters["id"]?.toIntOrNull()) {
                "Database Id"
            }
            val databaseModel = databaseRepository.find(id)
            if (databaseModel != null)
                call.respond(HttpStatusCode.OK, databaseModel)
            else
                call.respond(HttpStatusCode.NotFound)
        }

        delete("/databases/{id}") {
            val id = requireNotNull(call.parameters["id"]?.toIntOrNull()) {
                "Database Id"
            }
            databaseRepository.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        get("/databases/{id}/tables") {
            val id = requireNotNull(call.parameters["id"]?.toIntOrNull()) {
                "Table Id"
            }
            val tables = tableRepository.findByDatabaseId(id)
            call.respond(HttpStatusCode.OK, tables)
        }

        get("/tables/{id}") {
            val id = requireNotNull(call.parameters["id"]?.toIntOrNull()) {
                "Table Id"
            }
            val columns = columnRepository.findByTableId(id)
            call.respond(HttpStatusCode.OK, columns)
        }

        post("/columns") {
            val columnIds = call.receive<List<Int>>()
            val columns = columnRepository.findByIdList(columnIds)
            call.respond(HttpStatusCode.OK, columns)
        }

        get ("/archive") {
            val queries = pageQueryRepository.findAll()
            call.respond(HttpStatusCode.OK, queries)
        }

        post("/archive") {
            val pageQueryModel = call.receive<String>()
            pageQueryRepository.save(pageQueryModel)
            call.respond(HttpStatusCode.OK)
        }

        post("/query") {
            val pageQueryModel = call.receive<PageQueryModel>()
            val result = metadataService.queryPage(pageQueryModel)
            call.respond(HttpStatusCode.OK, result)
        }

        post("/upload") {
            var fileName = ""
            val contentLength = requireNotNull(call.request.header(HttpHeaders.ContentLength)?.toIntOrNull()) {
                "Content-Length header value"
            }
            require(contentLength < TEN_MEGABYTES) {
                "File size must be less than 10 MB"
            }
            call.receiveMultipart().forEachPart {partData ->
                when(partData) {
                    is PartData.FileItem -> {
                        fileName = partData.originalFileName ?: UUID.randomUUID().toString().plus(".db")
                        val bytes = partData.streamProvider().readBytes()
                        File("db/${fileName}").writeBytes(bytes)
                    }
                    else -> {}
                }
            }
            val connectionModel = DatabaseConnectionModel("jdbc:sqlite:db/${fileName}")
            metadataService.fetchSchema(connectionModel)
            call.respond(HttpStatusCode.Created)
        }
    }
}
