package metabank.model

import kotlinx.serialization.Serializable

@Serializable
data class ColumnRequestModel(
    val name: String
)

@Serializable
data class ColumnModel(
    val id: Int,
    val name: String
)