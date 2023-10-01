package metabank.table

import org.jetbrains.exposed.sql.Table

object Databases : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 128)
    val url = varchar("url", 1024)
    val username = varchar("username", 1024).nullable()
    val password = varchar("password", 1024).nullable()
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}