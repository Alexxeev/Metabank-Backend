package metabank.model

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConnectionModel(
    val url: String,
)
