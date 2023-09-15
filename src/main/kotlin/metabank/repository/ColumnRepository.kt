package metabank.repository

import metabank.model.ColumnRequestModel
import metabank.model.ColumnModel

interface ColumnRepository {
    suspend fun saveBatch(tableId: Int, columnBatchRequestModel: List<ColumnRequestModel>)
    suspend fun findByIdList(idList: List<Int>): List<ColumnModel>
    suspend fun findByTableId(tableId: Int): List<ColumnModel>
}