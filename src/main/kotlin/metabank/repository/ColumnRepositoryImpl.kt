package metabank.repository

import metabank.table.Columns
import metabank.table.Tables
import metabank.model.ColumnRequestModel
import metabank.model.ColumnModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select

class ColumnRepositoryImpl(database: Database) : ColumnRepository, BaseRepository(database) {
    private fun ResultRow.toModel(): ColumnModel = ColumnModel(
        id = this[Columns.id],
        name = this[Columns.name]
    )

    override suspend fun saveBatch(tableId: Int, columnBatchRequestModel: List<ColumnRequestModel>): Unit = query {
        require(Tables.select { Tables.id eq tableId }.count() > 0) { "Invalid Table ID" }
        Columns.batchInsert(
            columnBatchRequestModel,
            shouldReturnGeneratedValues = false
        ) { model ->
            this[Columns.name] = model.name
            this[Columns.tableId] = tableId
        }
    }

    override suspend fun findByIdList(idList: List<Int>): List<ColumnModel> = query {
        val columns = Columns.select( Columns.id inList idList).map { it.toModel() }
        check(columns.size == idList.size)
        return@query columns
    }

    override suspend fun findByTableId(tableId: Int): List<ColumnModel> = query {
        return@query Columns.select { Columns.tableId eq tableId }.map { it.toModel() }
    }
}