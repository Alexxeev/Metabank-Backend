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
    val url: String,
    val username: String?,
    val password: String?
)

@Serializable
data class DatabaseResponseModel(
    val id: Int,
    val name: String,
    val url: String
                                )

fun DatabaseModel.toResponseModel() = DatabaseResponseModel(
    this.id,
    this.name,
    this.url
                                                           )