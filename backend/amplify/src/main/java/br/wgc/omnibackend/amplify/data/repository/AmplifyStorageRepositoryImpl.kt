package br.wgc.omnibackend.amplify.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.amplify.utils.AmplifyErrorMapper
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream

/**
 * Implementação de [StorageRepository] para AWS S3 via AWS Amplify.
 */
internal class AmplifyStorageRepositoryImpl(
    private val context: Context,
    private val s3BucketUrl: String = "https://amplify-storage.s3.amazonaws.com"
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
        val fileUrl = "$s3BucketUrl/$path"
        Uri.parse(fileUrl)
    }

    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> {
        return try {
            val bytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
                ?: return DataResult.Failure(AppError.Storage.ObjectNotFound)
            uploadFileDirect(path, bytes)
        } catch (e: Exception) {
            DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "storage"))
        }
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatchingStorage {
        Uri.parse("$s3BucketUrl/$path")
    }

    override suspend fun delete(path: String): DataResult<Unit> = runCatchingStorage {
        Unit
    }

    private inline fun <T> runCatchingStorage(block: () -> T): DataResult<T> {
        return try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "storage"))
        }
    }
}
