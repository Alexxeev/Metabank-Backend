package metabank.repository

import metabank.model.DatabaseConnectionModel
import metabank.table.Databases
import metabank.model.DatabaseRequestModel
import metabank.model.DatabaseModel
import metabank.table.Databases.password
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class DatabaseRepositoryImpl(database: Database) : DatabaseRepository, BaseRepository(database) {
    private fun ResultRow.toModel(): DatabaseModel = DatabaseModel(
        id = this[Databases.id],
        name = this[Databases.name],
        url = this[Databases.url],
        username = this[Databases.username],
        password = this[Databases.password]
    )

    override suspend fun exists(name: String): Boolean = query {
        return@query Databases.select { Databases.name eq name }.count() > 0
    }

    override suspend fun save(
        databaseModel: DatabaseRequestModel,
        databaseConnectionModel: DatabaseConnectionModel): Int = query {
        return@query Databases.insert {
            it[name] = databaseModel.name
            it[url] = databaseModel.url
            it[username] = databaseConnectionModel.username
            it[password] = databaseConnectionModel.password
        }[Databases.id]
    }

    override suspend fun findAll(): List<DatabaseModel> = query {
        return@query Databases.selectAll().map { it.toModel() }
    }

    override suspend fun find(id: Int): DatabaseModel? = query {
        return@query Databases.select { Databases.id eq id }.singleOrNull()?.toModel()
    }

    override suspend fun delete(id: Int): Unit = query {
        Databases.deleteWhere { Databases.id eq id }
    }
}