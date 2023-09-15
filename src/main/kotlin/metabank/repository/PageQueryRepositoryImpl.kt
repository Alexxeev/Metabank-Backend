package metabank.repository

import kotlinx.serialization.json.Json
import metabank.model.PageQueryModel
import metabank.table.PageQueries
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class PageQueryRepositoryImpl(database: Database) : PageQueryRepository, BaseRepository(database) {
    private fun ResultRow.toModel(): PageQueryModel = Json.decodeFromString(this[PageQueries.query])

    override suspend fun find(id: Int): PageQueryModel? = query {
        return@query PageQueries.select { PageQueries.id eq id }.singleOrNull()?.toModel()
    }

    override suspend fun findAll(): List<PageQueryModel> = query {
        return@query PageQueries.selectAll().map { it.toModel() }
    }

    override suspend fun save(queryModel: String): Unit = query {
        PageQueries.insert {
            it[query] = queryModel
        }
    }

    override suspend fun delete(id: Int): Unit = query {
        PageQueries.deleteWhere { PageQueries.id eq id }
    }
}