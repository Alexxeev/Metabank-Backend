package metabank.repository

import metabank.model.TableRequestModel
import metabank.model.TableModel

interface TableRepository {
    suspend fun saveBatch(databaseId: Int, batchRequestModel: List<TableRequestModel>): List<TableModel>
    suspend fun findById(id: Int): TableModel?
    suspend fun findByDatabaseId(databaseId: Int): List<TableModel>
}