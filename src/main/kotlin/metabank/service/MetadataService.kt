package metabank.service

import metabank.exception.NoSuchDatabaseException
import metabank.model.DatabaseConnectionModel
import metabank.model.PageQueryModel
import metabank.model.PageQueryResultModel
import metabank.repository.ColumnRepository
import metabank.repository.DatabaseRepository
import metabank.repository.MetadataRepository
import metabank.repository.TableRepository
import org.jetbrains.exposed.sql.Database

class MetadataService(
    private val metadataRepository: MetadataRepository,
    private val databaseRepository: DatabaseRepository,
    private val tableRepository: TableRepository,
    private val columnRepository: ColumnRepository
                     ) {
    private val databases: MutableMap<String, Database> = mutableMapOf()

    private fun connectToDatabase(url: String, username: String? = null, password: String? = null): Database {
        try {

            return databases[url] ?: Database.connect(
                url = url,
                driver = "org.postgresql.Driver",
                user = username ?: "",
                password = password ?: "").also {
                databases[url] = it
            }
        } catch (e: IllegalStateException) {
            throw NoSuchDatabaseException(url, e)
        }
    }

    suspend fun fetchSchema(connectionModel: DatabaseConnectionModel): Boolean {
        val database = connectToDatabase(connectionModel.url, connectionModel.username, connectionModel.password)
        val databaseModel = metadataRepository.fetchDatabaseName(database)
        if (databaseRepository.exists(databaseModel.name))
            return false
        val databaseId = databaseRepository.save(databaseModel, connectionModel)
        val tables = metadataRepository.fetchTableNames(database)
        tableRepository.saveBatch(databaseId, tables).forEach { tableModel->
            val columns = metadataRepository.fetchColumnNames(database ,tableModel.name)
            columnRepository.saveBatch(tableModel.id, columns)
        }
        return true
    }

    suspend fun queryPage(pageQueryModel: PageQueryModel):
            PageQueryResultModel {
        val databaseModel = databaseRepository
            .find(pageQueryModel.databaseId)
            ?: throw IllegalArgumentException("Database Id: ${pageQueryModel.databaseId}")
        val database = connectToDatabase(databaseModel.url, databaseModel.username, databaseModel.password)
        val tableName = tableRepository
            .findById(pageQueryModel.tableId)
            ?.name
            ?: throw IllegalArgumentException("Table Id: ${pageQueryModel.tableId}")
        val columns = pageQueryModel
            .columnIds
            .takeIf { it.isNotEmpty() }?.let { columnIds ->
                columnRepository
                    .findByIdList(columnIds)
                    .map { it.name }
            } ?: listOf()
        return metadataRepository
            .queryPage(
                database,
                tableName,
                columns,
                pageQueryModel.numberOfElements,
                pageQueryModel.offset
            )
    }

    suspend fun dropDatabase(id: Int) {
        val databaseModel = checkNotNull(databaseRepository.find(id)) {
            "Database with provided id does not exist"
        }
        databases[databaseModel.url]?.let {
            databases.remove(databaseModel.url)
        }
        databaseRepository.delete(databaseModel.id)
    }
}