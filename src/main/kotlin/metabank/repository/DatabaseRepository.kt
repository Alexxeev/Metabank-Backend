package metabank.repository

import metabank.model.DatabaseRequestModel
import metabank.model.DatabaseModel

interface DatabaseRepository {
    suspend fun save(databaseModel: DatabaseRequestModel): Int
    suspend fun exists(name: String): Boolean
    suspend fun findAll(): List<DatabaseModel>
    suspend fun find(id: Int): DatabaseModel?
    suspend fun delete(id: Int)
}