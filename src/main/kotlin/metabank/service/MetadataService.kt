package metabank.service

import java.sql.Connection
import metabank.exception.NoSuchDatabaseException
import metabank.model.PageQueryModel
import metabank.model.PageQueryResultModel
import metabank.repository.ColumnRepository
import metabank.repository.DatabaseRepository
import metabank.repository.MetadataRepository
import metabank.repository.TableRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class MetadataService(
    private val metadataRepository: MetadataRepository,
    private val databaseRepository: DatabaseRepository,
    private val tableRepository: TableRepository,
    private val columnRepository: ColumnRepository
                     ) {
    private fun connectToDatabase(url: String): Database {
        try {
            return Database.connect(
                url = url,
                driver = "org.sqlite.JDBC"
            ).also {
                transaction(it) {
                    TransactionManager.manager.defaultIsolationLevel =
                        Connection.TRANSACTION_SERIALIZABLE
                }
            }
        } catch (e: IllegalStateException) {
            throw NoSuchDatabaseException(url, e)
        }
    }

    suspend fun fetchSchema(connectionUrl: String): Boolean {
        val database = connectToDatabase(connectionUrl)
        val databaseModel = metadataRepository.fetchDatabaseName(database)
        if (databaseRepository.exists(databaseModel.name))
            return false
        val databaseId = databaseRepository.save(databaseModel)
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
        val database = connectToDatabase(databaseModel.url)
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
}