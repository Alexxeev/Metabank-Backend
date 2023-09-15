package metabank.repository

import metabank.table.Tables
import metabank.model.TableRequestModel
import metabank.model.TableModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select

class TableRepositoryImpl(database: Database) : TableRepository, BaseRepository(database) {
    private fun ResultRow.toModel(): TableModel = TableModel(
        id = this[Tables.id],
        name = this[Tables.name]
    )

    override suspend fun saveBatch(
        databaseId: Int,
        batchRequestModel: List<TableRequestModel>
                                    ): List<TableModel> = query {
        return@query Tables.batchInsert(batchRequestModel) {model ->
            this[Tables.name] = model.name
            this[Tables.databaseId] = databaseId
        }.map { it.toModel() }
    }

    override suspend fun findById(id: Int): TableModel? = query {
        return@query Tables.select { Tables.id eq id }.singleOrNull()?.toModel()
    }

    override suspend fun findByDatabaseId(databaseId: Int): List<TableModel> = query {
        return@query Tables.select { Tables.databaseId eq databaseId }.map { it.toModel() }
    }
}