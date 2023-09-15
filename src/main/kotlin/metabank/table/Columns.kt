package metabank.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Columns : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 128)
    val tableId = reference("table_id", Tables.id, ReferenceOption.CASCADE)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}