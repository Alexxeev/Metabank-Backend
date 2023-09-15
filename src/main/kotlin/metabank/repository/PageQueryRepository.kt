package metabank.repository

import metabank.model.PageQueryModel

interface PageQueryRepository {
    suspend fun find(id: Int): PageQueryModel?
    suspend fun findAll(): List<PageQueryModel>
    suspend fun save(queryModel: String)
    suspend fun delete(id: Int)
}