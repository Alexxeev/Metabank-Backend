package metabank.model

import kotlinx.serialization.Serializable

@Serializable
data class PageQueryModel(
    val databaseId: Int,
    val tableId: Int,
    val columnIds: List<Int> = listOf(),
    val numberOfElements: Int,
    val offset: Int
                         )
