package br.wgc.omnibackend.firebase.data.repository

import android.net.Uri
import br.wgc.omnibackend.firebase.domain.repository.StorageRepository
import br.wgc.omnibackend.firebase.utils.AppError
import br.wgc.omnibackend.firebase.utils.DataResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override fun uploadFile(
        path: String,
        fileData: ByteArray
    ): Flow<DataResult<Uri>> = flow {
        val storageRef = storage.getReference(path)
        val result = runCatching {
            val uploadTask = storageRef.putBytes(fileData)
            uploadTask.await().storage.downloadUrl.await()
        }.fold(
            onSuccess = { downloadUri -> DataResult.Success(downloadUri) },
            onFailure = { exception -> DataResult.Failure(toMapperError(exception)) }
        )
        emit(result)
    }

    override suspend fun uploadFile(path: String, fileUri: Uri): DataResult<Uri> = runCatching {
        val storageRef = storage.getReference(path)
        val uploadTask = storageRef.putFile(fileUri)
        val downloadUri = uploadTask.await().storage.downloadUrl.await()
        DataResult.Success(downloadUri)
    }.getOrElse { exception ->
        DataResult.Failure(toMapperError(exception))
    }

    override suspend fun uploadFile(path: String, inputStream: java.io.InputStream): DataResult<Uri> = runCatching {
        val storageRef = storage.getReference(path)
        val uploadTask = storageRef.putStream(inputStream)
        val downloadUri = uploadTask.await().storage.downloadUrl.await()
        DataResult.Success(downloadUri)
    }.getOrElse { exception ->
        DataResult.Failure(toMapperError(exception))
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatching {
        val downloadUri = storage.getReference(path).downloadUrl.await()
        DataResult.Success(downloadUri)
    }.getOrElse { exception ->
        DataResult.Failure(toMapperError(exception))
    }

    override fun getFileUrl(path: String): Flow<DataResult<Uri>> = flow {
        val result = runCatching {
            storage.getReference(path).downloadUrl.await()
        }.fold(
            onSuccess = { downloadUri -> DataResult.Success(downloadUri) },
            onFailure = { exception -> DataResult.Failure(toMapperError(exception)) }
        )
        emit(result)
    }

    override fun listFiles(folderPath: String): Flow<DataResult<List<Uri>>> = flow {
        val result = runCatching {
            val listResult = storage.getReference(folderPath).listAll().await()
            listResult.items.map { it.downloadUrl.await() }
        }.fold(
            onSuccess = { uris -> DataResult.Success(uris) },
            onFailure = { exception -> DataResult.Failure(toMapperError(exception)) }
        )
        emit(result)
    }

    override suspend fun delete(path: String): DataResult<Unit> = runCatching {
        storage.getReference(path).delete().await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        DataResult.Failure(toMapperError(exception))
    }

    override fun deleteFile(path: String): Flow<DataResult<Unit>> = flow {
        val result = runCatching {
            storage.getReference(path).delete().await()
            Unit
        }.fold(
            onSuccess = { DataResult.Success(Unit) },
            onFailure = { exception -> DataResult.Failure(toMapperError(exception)) }
        )
        emit(result)
    }

    /**
     * Função de extensão privada que mapeia uma exceção para um [AppError] específico.
     */
    private fun toMapperError(exception: Throwable): AppError {
        return when (exception) {
            is StorageException -> exception.toStorageError()
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
    }

    /**
     * Mapeia uma [StorageException] para um [AppError] específico, tratando Storage e Erros Genéricos.
     */
    private fun StorageException.toStorageError(): AppError {
        return when (this.errorCode) {
            StorageException.ERROR_OBJECT_NOT_FOUND -> AppError.Storage.ObjectNotFound
            StorageException.ERROR_BUCKET_NOT_FOUND -> AppError.Storage.BucketNotFound
            StorageException.ERROR_PROJECT_NOT_FOUND -> AppError.Storage.ProjectNotFound
            StorageException.ERROR_QUOTA_EXCEEDED -> AppError.Storage.QuotaExceeded
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> AppError.Generic.Network
            StorageException.ERROR_CANCELED -> AppError.Storage.UploadCancelled
            StorageException.ERROR_INVALID_CHECKSUM -> AppError.Storage.DownloadFailed
            StorageException.ERROR_NOT_AUTHENTICATED,
            StorageException.ERROR_NOT_AUTHORIZED -> AppError.Storage.PermissionDenied
            else -> AppError.Storage.Generic(exception = this)
        }
    }
}

