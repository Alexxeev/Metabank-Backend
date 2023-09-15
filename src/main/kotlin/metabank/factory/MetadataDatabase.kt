package metabank.factory

import metabank.table.Columns
import metabank.table.Databases
import metabank.table.PageQueries
import metabank.table.Tables
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object MetadataDatabase {
    val instance: Database by lazy {
        Database.connect(
            url = "jdbc:sqlite:data.db?foreign_keys=ON",
            driver = "org.sqlite.JDBC"
        ).also {
            transaction(it) {
                TransactionManager.manager.defaultIsolationLevel =
                    Connection.TRANSACTION_SERIALIZABLE
                SchemaUtils.create(Databases, Tables, Columns, PageQueries)
            }
        }
    }
}