package br.wgc.omnibackend.amplify.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.amplify.utils.AmplifyErrorMapper
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.storage.StoragePath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Implementação corporativa de [StorageRepository] para AWS S3 via AWS Amplify.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
internal class AmplifyStorageRepositoryImpl(private val context: Context) : StorageRepository {

    override fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>> = flow {
        emit(uploadFileDirect(path, fileData))
    }

    override fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>> = flow {
        emit(uploadFileDirect(path, fileUri))
    }

    override fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>> = flow {
        val result = runCatchingStorage {
            val storagePath = StoragePath.fromString(path)
            Amplify.Storage.uploadInputStream(storagePath, inputStream)
            val downloadUrlResult = Amplify.Storage.getUrl(storagePath)
            Uri.parse(downloadUrlResult.url.toString())
        }
        emit(result)
    }

    override suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri> = runCatchingStorage {
        val stream = ByteArrayInputStream(fileData)
        val storagePath = StoragePath.fromString(path)
        Amplify.Storage.uploadInputStream(storagePath, stream)
        val downloadUrlResult = Amplify.Storage.getUrl(storagePath)
        Uri.parse(downloadUrlResult.url.toString())
    }

    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return DataResult.Failure(AppError.Storage.ObjectNotFound)
            val stream = inputStream.use { ByteArrayInputStream(it.readBytes()) }
            val storagePath = StoragePath.fromString(path)
            Amplify.Storage.uploadInputStream(storagePath, stream)
            val downloadUrlResult = Amplify.Storage.getUrl(storagePath)
            DataResult.Success(Uri.parse(downloadUrlResult.url.toString()))
        } catch (e: Exception) {
            DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "storage"))
        }
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatchingStorage {
        val storagePath = StoragePath.fromString(path)
        val downloadUrlResult = Amplify.Storage.getUrl(storagePath)
        Uri.parse(downloadUrlResult.url.toString())
    }

    override suspend fun delete(path: String): DataResult<Unit> = runCatchingStorage {
        val storagePath = StoragePath.fromString(path)
        Amplify.Storage.remove(storagePath)
    }

    private suspend inline fun <T> runCatchingStorage(crossinline block: suspend () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "storage"))
    }
}
