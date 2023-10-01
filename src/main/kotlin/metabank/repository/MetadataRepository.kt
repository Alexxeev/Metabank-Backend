package metabank.repository

import java.sql.ResultSet
import kotlinx.coroutines.Dispatchers
import metabank.model.ColumnRequestModel
import metabank.model.DatabaseRequestModel
import metabank.model.PageQueryResultModel
import metabank.model.TableRequestModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface MetadataRepository {
    suspend fun <T> query(db: Database, block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.Default, db) { block() }

    suspend fun fetchDatabaseName(database: Database): DatabaseRequestModel = query(database) {
        return@query DatabaseRequestModel(database.name, database.url)
    }

    suspend fun fetchTableNames(database: Database): List<TableRequestModel> = query(database) {
        return@query database
            .dialect
            .allTablesNames()
            .map { TableRequestModel(it) }
    }
    fun ResultSet.toNamesList(): List<String> = buildList {
        while (next()) add(getString(1))
    }

    suspend fun fetchColumnNames(database: Database, tableName: String): List<ColumnRequestModel>

    suspend fun queryTotal(database: Database, tableName: String): Int = query(database) {
        val sql = "SELECT count(*) FROM $tableName"
        val statement = database.connector().prepareStatement(sql, false)
        return@query statement
            .executeQuery()
            .apply { next() }
            .getInt(1)
            .also { statement.closeIfPossible() }
    }
    fun ResultSet.toQueryResultRowList(): List<Map<String, String>> = buildList {
        while (next()) add(toQueryResultRowFromCurrentCursor())
    }

    fun ResultSet.toQueryResultRowFromCurrentCursor(): Map<String, String> = buildMap {
        for (i in 1..metaData.columnCount)
            put(
                metaData.getColumnName(i),
                getObject(i)?.toString() ?: ""
               )
    }

    suspend fun queryPage(
        database: Database,
        tableName: String,
        columns: List<String>,
        pageSize: Int,
        pageNumber: Int
                         ): PageQueryResultModel = query(database) {
        val columnString = columns
            .takeIf { it.isNotEmpty() }
            ?.joinToString()
            ?: "*"
        val sql = "SELECT $columnString FROM $tableName LIMIT ? OFFSET ?"
        val statement = database.connector().prepareStatement(sql, false).apply {
            fillParameters(
                listOf(
                    Pair(IntegerColumnType(), pageSize),
                    Pair(IntegerColumnType(), pageNumber)
                      )
                          )
        }
        val rows = statement.executeQuery().toQueryResultRowList()
        return@query PageQueryResultModel(
            rows,
            queryTotal(database, tableName)
                                         ).also {
            statement.closeIfPossible()
        }
    }
}