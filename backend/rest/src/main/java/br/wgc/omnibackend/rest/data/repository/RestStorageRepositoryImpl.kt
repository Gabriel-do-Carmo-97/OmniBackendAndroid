package br.wgc.omnibackend.rest.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.core.network.OmniHttpClientFactory
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.rest.utils.RestErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

/**
 * Implementação de [StorageRepository] enviando arquivos para endpoints REST (`/api/v1/storage/{path}`).
 */
internal class RestStorageRepositoryImpl(
    private val context: Context,
    private val baseUrl: String,
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
        val urlString = "$baseUrl/api/v1/storage/$path"
        val body = fileData.toRequestBody("application/octet-stream".toMediaType())
        val request = Request.Builder().url(urlString).post(body).build()
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                Uri.parse(urlString)
            } else {
                throw IllegalStateException("REST Storage Upload HTTP ${response.code}: ${response.message}")
            }
        }
    }

    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> {
        return try {
            val bytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
                ?: return DataResult.Failure(AppError.Storage.ObjectNotFound)
            uploadFileDirect(path, bytes)
        } catch (e: Exception) {
            DataResult.Failure(RestErrorMapper.mapThrowable(e, "storage"))
        }
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatchingStorage {
        Uri.parse("$baseUrl/api/v1/storage/$path")
    }

    override suspend fun delete(path: String): DataResult<Unit> = runCatchingStorage {
        val urlString = "$baseUrl/api/v1/storage/$path"
        val request = Request.Builder().url(urlString).delete().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("REST Storage Delete HTTP ${response.code}: ${response.message}")
            }
        }
    }

    private inline fun <T> runCatchingStorage(block: () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Failure(RestErrorMapper.mapThrowable(e, "storage"))
    }
}
