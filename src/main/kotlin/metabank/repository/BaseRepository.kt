package metabank.repository

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

open class BaseRepository(private val database: Database) {
    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}