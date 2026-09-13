package br.wgc.omnibackend.testing

import android.net.Uri
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Implementação em memória de [StorageRepository] para testes unitários e instrumentados.
 * Armazena bytes e simula URLs de download sem conexão externa.
 */
class FakeStorageRepository : StorageRepository {

    private val storageMap = mutableMapOf<String, ByteArray>()

    /** Permite injetar um erro simulado para testar fluxos de falha. */
    var simulatedError: AppError? = null

    override fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>> = flow {
        emit(uploadFileDirect(path, fileData))
    }

    override fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>> = flow {
        emit(uploadFileDirect(path, fileUri))
    }

    override fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>> = flow {
        val buffer = ByteArrayOutputStream()
        inputStream.copyTo(buffer)
        emit(uploadFileDirect(path, buffer.toByteArray()))
    }

    override suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri> {
        simulatedError?.let { return DataResult.Failure(it) }
        storageMap[path] = fileData
        return DataResult.Success(Uri.parse("https://fake-storage.local/$path"))
    }

    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> {
        simulatedError?.let { return DataResult.Failure(it) }
        storageMap[path] = fileUri.toString().toByteArray()
        return DataResult.Success(Uri.parse("https://fake-storage.local/$path"))
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> {
        simulatedError?.let { return DataResult.Failure(it) }
        return if (storageMap.containsKey(path)) {
            DataResult.Success(Uri.parse("https://fake-storage.local/$path"))
        } else {
            DataResult.Failure(AppError.Storage.ObjectNotFound)
        }
    }

    override suspend fun delete(path: String): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        storageMap.remove(path)
        return DataResult.Success(Unit)
    }
}
