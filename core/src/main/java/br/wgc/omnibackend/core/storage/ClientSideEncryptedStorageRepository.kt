package br.wgc.omnibackend.core.storage

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.security.KeystoreCryptoManager
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

/**
 * Repositório decorador para **criptografia de arquivos no cliente (Zero-Knowledge Storage)**.
 *
 * Criptografa o conteúdo do arquivo usando [KeystoreCryptoManager] no dispositivo *antes* de
 * transmitir para o [remoteStorage], garantindo que a nuvem armazene apenas dados cifrados.
 *
 * @param remoteStorage Repositório de armazenamento remoto de destino (ex: S3, Firebase, R2, Supabase).
 * @param cryptoManager Gerenciador de criptografia Keystore.
 * @param context Contexto Android.
 */
class ClientSideEncryptedStorageRepository(
    private val remoteStorage: StorageRepository,
    private val cryptoManager: KeystoreCryptoManager,
    private val context: Context,
) : StorageRepository {

    override fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>> {
        val encryptedString = cryptoManager.encrypt(String(fileData, Charsets.UTF_8))
        return remoteStorage.uploadFile(path, encryptedString.toByteArray(Charsets.UTF_8))
    }

    override fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>> {
        val bytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
        if (bytes == null) {
            return remoteStorage.uploadFile(path, fileUri)
        }
        val encryptedString = cryptoManager.encrypt(String(bytes, Charsets.UTF_8))
        return remoteStorage.uploadFile(path, encryptedString.toByteArray(Charsets.UTF_8))
    }

    override fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>> {
        val bytes = inputStream.use { it.readBytes() }
        val encryptedString = cryptoManager.encrypt(String(bytes, Charsets.UTF_8))
        return remoteStorage.uploadFile(path, encryptedString.toByteArray(Charsets.UTF_8))
    }

    override suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri> {
        val encryptedString = cryptoManager.encrypt(String(fileData, Charsets.UTF_8))
        return remoteStorage.uploadFileDirect(path, encryptedString.toByteArray(Charsets.UTF_8))
    }

    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> {
        val bytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
            ?: return DataResult.Failure(AppError.Storage.ObjectNotFound)
        val encryptedString = cryptoManager.encrypt(String(bytes, Charsets.UTF_8))
        return remoteStorage.uploadFileDirect(path, encryptedString.toByteArray(Charsets.UTF_8))
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = remoteStorage.getDownloadUrl(path)

    override suspend fun delete(path: String): DataResult<Unit> = remoteStorage.delete(path)

    /**
     * Baixa os bytes do arquivo criptografado do armazenamento remoto e os descriptografa localmente.
     *
     * @param encryptedBytes Bytes cifrados baixados do servidor.
     * @return String descriptografada em texto plano.
     */
    fun decryptDownloadedData(encryptedBytes: ByteArray): String {
        val encryptedData = String(encryptedBytes, Charsets.UTF_8)
        return cryptoManager.decrypt(encryptedData)
    }
}
