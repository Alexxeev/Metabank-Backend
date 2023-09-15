package metabank.repository

import metabank.model.ColumnRequestModel
import metabank.model.DatabaseRequestModel
import metabank.model.PageQueryResultModel
import metabank.model.TableRequestModel
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.ResultSet

class MetadataRepository {
    private suspend fun <T> query(db: Database, block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, db) { block() }

    suspend fun fetchDatabaseName(database: Database): DatabaseRequestModel = query(database) {
        return@query DatabaseRequestModel(database.name, database.url)
    }

    suspend fun fetchTableNames(database: Database): List<TableRequestModel> = query(database) {
        return@query database
            .dialect
            .allTablesNames()
            .map { TableRequestModel(it) }
    }

    private fun ResultSet.toColumnNamesList(): List<String> = buildList {
        while (next()) add(getString(1))
    }

    suspend fun fetchColumnNames(database: Database, tableName: String): List<ColumnRequestModel> = query(database) {
        val sql = "SELECT name FROM PRAGMA_TABLE_INFO(?)"
        val statement = database.connector().prepareStatement(sql, false).apply {
            fillParameters(listOf(Pair(VarCharColumnType(), tableName)))
        }
        return@query statement
            .executeQuery()
            .toColumnNamesList()
            .map { ColumnRequestModel(it) }
    }

    private suspend fun queryTotal(database: Database, tableName: String): Int = query(database) {
        val sql = "SELECT count(*) FROM $tableName"
        database
            .connector()
            .prepareStatement(sql, false)
            .executeQuery()
            .getInt(1)

    }

    private fun ResultSet.toQueryResultRowList(): List<Map<String, String>> = buildList {
        while (next()) add(toQueryResultRowFromCurrentCursor())
    }

    private fun ResultSet.toQueryResultRowFromCurrentCursor(): Map<String, String> = buildMap {
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
        )
    }
}