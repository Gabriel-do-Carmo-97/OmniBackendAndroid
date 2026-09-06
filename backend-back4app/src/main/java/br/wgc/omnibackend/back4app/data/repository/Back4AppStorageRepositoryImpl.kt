package br.wgc.omnibackend.back4app.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.back4app.utils.Back4AppErrorMapper
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.DataResult
import com.parse.ParseException
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream

/**
 * Implementação concreta de [StorageRepository] para Back4App utilizando [ParseFile].
 */
internal class Back4AppStorageRepositoryImpl(
    private val context: Context,
    private val defaultClassName: String = "Files"
) : StorageRepository {

    override fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>> = flow {
        emit(uploadFileDirect(path, fileData))
    }

    override fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>> = flow {
        emit(uploadFileDirect(path, fileUri))
    }

    override fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>> = flow {
        val bytes = inputStream.use { it.readBytes() }
        emit(uploadFileDirect(path, bytes))
    }

    override suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri> = runCatchingStorage {
        val parseFile = ParseFile(path, fileData)
        parseFile.save()
        val parseObject = ParseObject(defaultClassName)
        parseObject.put("name", path)
        parseObject.put("file", parseFile)
        parseObject.save()
        Uri.parse(parseFile.url)
    }

    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> = runCatchingStorage {
        val bytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Unable to open stream for URI: $fileUri")
        uploadFileDirect(path, bytes)
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatchingStorage {
        val query = ParseQuery.getQuery<ParseObject>(defaultClassName)
        query.whereEqualTo("name", path)
        val obj = query.first
        val file = obj.getParseFile("file") ?: throw ParseException(ParseException.OBJECT_NOT_FOUND, "File not found")
        Uri.parse(file.url)
    }

    override suspend fun delete(path: String): DataResult<Unit> = runCatchingStorage {
        val query = ParseQuery.getQuery<ParseObject>(defaultClassName)
        query.whereEqualTo("name", path)
        val obj = query.first
        obj.delete()
        Unit
    }

    private inline fun <T> runCatchingStorage(block: () -> T): DataResult<T> {
        return try {
            DataResult.Success(block())
        } catch (e: ParseException) {
            DataResult.Failure(Back4AppErrorMapper.mapException(e, "storage"))
        } catch (e: Exception) {
            DataResult.Failure(Back4AppErrorMapper.mapThrowable(e, "storage"))
        }
    }
}
