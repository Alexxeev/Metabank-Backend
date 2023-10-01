package metabank.repository

import metabank.model.ColumnRequestModel
import metabank.model.TableRequestModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.VarCharColumnType

class PostgreMetadataRepository : MetadataRepository {
    override suspend fun fetchTableNames(database: Database): List<TableRequestModel> {
        val sql = """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema='public'
            AND table_type='BASE TABLE';
        """.trimIndent()
        val statement = database.connector().prepareStatement(sql, false)
        return statement
            .executeQuery()
            .toNamesList()
            .map { TableRequestModel(it) }
            .also { statement.closeIfPossible() }
    }
    override suspend fun fetchColumnNames(
        database: Database,
        tableName: String): List<ColumnRequestModel> = query(database) {
        val sql = """
            select column_name
            from information_schema.columns
            where table_name = ?
            order by ordinal_position;
        """.trimIndent()
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