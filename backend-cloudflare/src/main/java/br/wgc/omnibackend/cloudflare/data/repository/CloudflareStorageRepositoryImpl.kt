package br.wgc.omnibackend.cloudflare.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.cloudflare.utils.CloudflareErrorMapper
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Implementação de [StorageRepository] enviando arquivos para Cloudflare R2 via Worker R2 endpoint (`/r2/*`).
 */
internal class CloudflareStorageRepositoryImpl(
    private val context: Context,
    private val workerBaseUrl: String
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
        val urlString = "$workerBaseUrl/r2/$path"
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            doOutput = true
            setRequestProperty("Content-Type", "application/octet-stream")
            connectTimeout = 15000
            readTimeout = 15000
        }
        conn.outputStream.use { it.write(fileData) }
        val code = conn.responseCode
        if (code in 200..299) {
            Uri.parse(urlString)
        } else {
            throw IllegalStateException("Cloudflare R2 Upload HTTP $code: ${conn.responseMessage}")
        }
    }

    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> {
        return try {
            val bytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
                ?: return DataResult.Failure(AppError.Storage.ObjectNotFound)
            uploadFileDirect(path, bytes)
        } catch (e: Exception) {
            DataResult.Failure(CloudflareErrorMapper.mapThrowable(e, "storage"))
        }
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatchingStorage {
        Uri.parse("$workerBaseUrl/r2/$path")
    }

    override suspend fun delete(path: String): DataResult<Unit> = runCatchingStorage {
        val urlString = "$workerBaseUrl/r2/$path"
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 10000
            readTimeout = 10000
        }
        conn.responseCode
        Unit
    }

    private inline fun <T> runCatchingStorage(block: () -> T): DataResult<T> {
        return try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Failure(CloudflareErrorMapper.mapThrowable(e, "storage"))
        }
    }
}
