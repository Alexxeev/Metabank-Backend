package metabank.service

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import metabank.repository.DatabaseRepository

const val WORK_DIR_NAME = "db"

class DatabaseFileService(
    private val databaseRepository: DatabaseRepository
                         ) {
    fun createWorkingDirectory() {
        val path = Path.of(WORK_DIR_NAME)
        if (Files.notExists(path)) {
            Files.createDirectory(path)
        }
    }
    private fun String.fullPath(): String = "$WORK_DIR_NAME/".plus(this@fullPath)
    fun exists(fileName: String): Boolean = Files.exists(Path.of(fileName.fullPath()))
    suspend fun delete(databaseId: Int) = withContext(Dispatchers.IO) {
        val databaseName = databaseRepository.find(databaseId)?.name ?: return@withContext
        val databasePath = Path.of(databaseName.fullPath())
        Files.deleteIfExists(databasePath)
    }

    fun create(fileName: String, inputStream: InputStream) {
        val bytes = inputStream.readBytes()
        File(fileName.fullPath()).writeBytes(bytes)
    }
}