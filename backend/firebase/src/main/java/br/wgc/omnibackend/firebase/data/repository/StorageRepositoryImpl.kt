package br.wgc.omnibackend.firebase.data.repository

import android.net.Uri
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

/**
 * Implementação do contrato [StorageRepository] utilizando o Google Cloud Storage for Firebase.
 *
 * Provê upload de arrays de bytes, arquivos locais e streams, com resolução da URL pública de download.
 *
 * @property storage Instância do [FirebaseStorage] injetada.
 */
class StorageRepositoryImpl @Inject constructor(private val storage: FirebaseStorage) : StorageRepository {

    /**
     * Envia um array de bytes para o caminho especificado e emite a [Uri] pública de download via [Flow].
     */
    override fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>> = flow {
        val storageRef = storage.getReference(path)
        val result = runCatching {
            val uploadTask = storageRef.putBytes(fileData)
            uploadTask.await().storage.downloadUrl.await()
        }.fold(
            onSuccess = { downloadUri -> DataResult.Success(downloadUri) },
            onFailure = { exception -> DataResult.Failure(toMapperError(exception)) },
        )
        emit(result)
    }

    /**
     * Envia um arquivo apontado por [Uri] local e emite o resultado via [Flow].
     */
    override fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>> = flow {
        val storageRef = storage.getReference(path)
        val result = runCatching {
            val uploadTask = storageRef.putFile(fileUri)
            uploadTask.await().storage.downloadUrl.await()
        }.fold(
            onSuccess = { downloadUri -> DataResult.Success(downloadUri) },
            onFailure = { exception -> DataResult.Failure(toMapperError(exception)) },
        )
        emit(result)
    }

    /**
     * Envia dados lidos de uma [InputStream] e emite o resultado via [Flow].
     */
    override fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>> = flow {
        val storageRef = storage.getReference(path)
        val result = runCatching {
            val uploadTask = storageRef.putStream(inputStream)
            uploadTask.await().storage.downloadUrl.await()
        }.fold(
            onSuccess = { downloadUri -> DataResult.Success(downloadUri) },
            onFailure = { exception -> DataResult.Failure(toMapperError(exception)) },
        )
        emit(result)
    }

    /**
     * Envia um array de bytes diretamente de forma suspensa.
     */
    override suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri> = runCatching {
        val storageRef = storage.getReference(path)
        val uploadTask = storageRef.putBytes(fileData)
        val downloadUri = uploadTask.await().storage.downloadUrl.await()
        DataResult.Success(downloadUri)
    }.getOrElse { exception ->
        DataResult.Failure(toMapperError(exception))
    }

    /**
     * Envia um arquivo local por [Uri] diretamente de forma suspensa.
     */
    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> = runCatching {
        val storageRef = storage.getReference(path)
        val uploadTask = storageRef.putFile(fileUri)
        val downloadUri = uploadTask.await().storage.downloadUrl.await()
        DataResult.Success(downloadUri)
    }.getOrElse { exception ->
        DataResult.Failure(toMapperError(exception))
    }

    /**
     * Recupera a URL pública de download de um arquivo existente no bucket.
     */
    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatching {
        val storageRef = storage.getReference(path)
        val downloadUri = storageRef.downloadUrl.await()
        DataResult.Success(downloadUri)
    }.getOrElse { exception ->
        DataResult.Failure(toMapperError(exception))
    }

    /**
     * Remove um arquivo do Firebase Storage.
     */
    override suspend fun delete(path: String): DataResult<Unit> = runCatching {
        val storageRef = storage.getReference(path)
        storageRef.delete().await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        DataResult.Failure(toMapperError(exception))
    }

    private fun toMapperError(exception: Throwable): AppError = when (exception) {
        is StorageException -> when (exception.errorCode) {
            StorageException.ERROR_OBJECT_NOT_FOUND -> AppError.Storage.ObjectNotFound
            StorageException.ERROR_BUCKET_NOT_FOUND -> AppError.Storage.BucketNotFound
            StorageException.ERROR_PROJECT_NOT_FOUND -> AppError.Storage.ProjectNotFound
            StorageException.ERROR_QUOTA_EXCEEDED -> AppError.Storage.QuotaExceeded
            StorageException.ERROR_NOT_AUTHENTICATED -> AppError.Storage.PermissionDenied
            StorageException.ERROR_NOT_AUTHORIZED -> AppError.Storage.PermissionDenied
            StorageException.ERROR_CANCELED -> AppError.Storage.UploadCancelled
            else -> AppError.Storage.Generic(exception)
        }
        is IOException -> AppError.Generic.Network
        is Exception -> AppError.Generic.Unknown(exception)
        else -> AppError.Generic.Unknown(exception)
    }
}
