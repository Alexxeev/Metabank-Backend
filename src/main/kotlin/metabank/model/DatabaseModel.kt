package metabank.model

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseRequestModel(
    val name: String,
    val url: String
)

@Serializable
data class DatabaseModel(
    val id: Int,
    val name: String,
    val url: String
)