package metabank.model

import kotlinx.serialization.Serializable

@Serializable
data class TableRequestModel(
    val name: String
)

@Serializable
data class TableModel(
    val id: Int,
    val name: String
)