package metabank.plugins

import metabank.repository.ColumnRepositoryImpl
import metabank.repository.DatabaseRepositoryImpl
import metabank.repository.MetadataRepository
import metabank.repository.TableRepositoryImpl
import metabank.factory.MetadataDatabase
import metabank.model.DatabaseConnectionModel
import metabank.model.PageQueryModel
import metabank.service.MetadataService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import metabank.repository.PageQueryRepositoryImpl

fun Application.configureRouting() {
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
            val id = call.parameters["id"]?.toInt()
                ?: throw IllegalArgumentException("Database Id")
            val databaseModel = databaseRepository.find(id)
            if (databaseModel != null)
                call.respond(HttpStatusCode.OK, databaseModel)
            else
                call.respond(HttpStatusCode.NotFound)
        }

        delete("/databases/{id}") {
            val id = call.parameters["id"]?.toInt()
                ?: throw IllegalArgumentException("Database Id")
            databaseRepository.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        get("/databases/{id}/tables") {
            val id = call.parameters["id"]?.toInt()
                ?: throw IllegalArgumentException("Table Id")
            val tables = tableRepository.findByDatabaseId(id)
            call.respond(HttpStatusCode.OK, tables)
        }

        get("/tables/{id}") {
            val id = call.parameters["id"]?.toInt()
                ?: throw IllegalArgumentException("Table Id")
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
    }
}