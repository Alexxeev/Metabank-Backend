package metabank.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Tables : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 128)
    val databaseId = reference("database_id", Databases.id, ReferenceOption.CASCADE)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}