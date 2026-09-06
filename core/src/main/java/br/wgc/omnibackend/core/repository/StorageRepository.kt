package br.wgc.omnibackend.core.repository

import android.net.Uri
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

/**
 * Contrato agnóstico para armazenamento de arquivos e objetos na nuvem.
 */
interface StorageRepository {
    fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>>
    fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>>
    fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>>

    suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri>
    suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri>
    suspend fun getDownloadUrl(path: String): DataResult<Uri>
    suspend fun delete(path: String): DataResult<Unit>
}
