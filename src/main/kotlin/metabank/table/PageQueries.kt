package metabank.table

import org.jetbrains.exposed.sql.Table

object PageQueries : Table() {
    val id = integer("id").autoIncrement()
    val query = text("query")
    override val primaryKey = PrimaryKey(id)
}