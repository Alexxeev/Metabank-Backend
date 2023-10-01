package metabank.repository

import metabank.model.ColumnRequestModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.VarCharColumnType

class SQLiteMetadataRepository : MetadataRepository {
    override suspend fun fetchColumnNames(
        database: Database,
        tableName: String): List<ColumnRequestModel> = query(database) {
        val sql = "SELECT name FROM PRAGMA_TABLE_INFO(?)"
        val statement = database.connector().prepareStatement(sql, false).apply {
            fillParameters(listOf(Pair(VarCharColumnType(), tableName)))
        }
        return@query statement
            .executeQuery()
            .toNamesList()
            .map { ColumnRequestModel(it) }
            .also { statement.closeIfPossible() }
    }
}