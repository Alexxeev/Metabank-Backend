package metabank.model

import kotlinx.serialization.Serializable

@Serializable
data class PageQueryResultModel(
    val rows: List<Map<String, String>>,
    val totalRows: Int
)
