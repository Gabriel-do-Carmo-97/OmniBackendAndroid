package br.wgc.omnibackend.cloudflare.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.cloudflare.utils.CloudflareErrorMapper
import br.wgc.omnibackend.core.network.OmniHttpClientFactory
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

/**
 * Implementação de [StorageRepository] enviando arquivos para Cloudflare R2 via Worker R2 endpoint.
 */
internal class CloudflareStorageRepositoryImpl(
    private val context: Context,
    private val workerBaseUrl: String,
    private val httpClient: OkHttpClient = OmniHttpClientFactory.createClient(),
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
        val body = fileData.toRequestBody("application/octet-stream".toMediaType())
        val request = Request.Builder().url(urlString).put(body).build()
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                Uri.parse(urlString)
            } else {
                throw IllegalStateException("Cloudflare R2 Upload HTTP ${response.code}: ${response.message}")
            }
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
        val request = Request.Builder().url(urlString).delete().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Cloudflare R2 Delete HTTP ${response.code}: ${response.message}")
            }
        }
    }

    private inline fun <T> runCatchingStorage(block: () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Failure(CloudflareErrorMapper.mapThrowable(e, "storage"))
    }
}
