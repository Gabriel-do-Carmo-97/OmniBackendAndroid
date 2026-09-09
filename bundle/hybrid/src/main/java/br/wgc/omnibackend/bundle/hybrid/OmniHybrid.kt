package br.wgc.omnibackend.bundle.hybrid

import br.wgc.omnibackend.core.hybrid.HybridAuthRepository
import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.DataResult
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

/**
 * Ponto de entrada de alto nível para orquestração híbrida de backends com suporte a failover e redundância.
 *
 * Permite parear livremente quaisquer duas implementações de backend (Firebase + Supabase,
 * Firebase + Back4App, PocketBase + REST, etc.) sem acoplamento direto a nenhum SDK específico.
 */
object OmniHybrid {

    /**
     * Cria um [AuthRepository] híbrido com failover automático do provedor primário para o secundário.
     *
     * @param primary Provedor primário de autenticação.
     * @param secondary Provedor secundário de autenticação (fallback).
     */
    fun createAuth(
        primary: AuthRepository,
        secondary: AuthRepository
    ): AuthRepository = HybridAuthRepository(primary, secondary)

    /**
     * Cria um [FirestoreRepository] híbrido com failover automático de banco NoSQL.
     *
     * @param primary Provedor primário de banco de dados.
     * @param secondary Provedor secundário de banco de dados (fallback).
     */
    fun createDatabase(
        primary: FirestoreRepository,
        secondary: FirestoreRepository
    ): FirestoreRepository = HybridDatabaseRepository(primary, secondary)

    /**
     * Cria um [StorageRepository] híbrido com failover automático de arquivos e buckets.
     *
     * @param primary Provedor primário de armazenamento.
     * @param secondary Provedor secundário de armazenamento (fallback).
     */
    fun createStorage(
        primary: StorageRepository,
        secondary: StorageRepository
    ): StorageRepository = HybridStorageRepository(primary, secondary)
}

/**
 * Implementação híbrida de [FirestoreRepository] com failover ativo-passivo entre dois provedores NoSQL.
 */
internal class HybridDatabaseRepository(
    private val primary: FirestoreRepository,
    private val secondary: FirestoreRepository
) : FirestoreRepository {

    override suspend fun <T : Any> addDocument(
        collection: String,
        data: T,
        customId: String?
    ): DataResult<String> {
        val res = primary.addDocument(collection, data, customId)
        return if (res is DataResult.Success) res else secondary.addDocument(collection, data, customId)
    }

    override suspend fun <T : Any> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): DataResult<T?> {
        val res = primary.getDocument(collection, documentId, clazz)
        return if (res is DataResult.Success) res else secondary.getDocument(collection, documentId, clazz)
    }

    override suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>
    ): DataResult<Unit> {
        val res = primary.updateDocument(collection, documentId, data)
        return if (res is DataResult.Success) res else secondary.updateDocument(collection, documentId, data)
    }

    override suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): DataResult<Unit> {
        val res = primary.deleteDocument(collection, documentId)
        return if (res is DataResult.Success) res else secondary.deleteDocument(collection, documentId)
    }

    override suspend fun <T : Any> findDocuments(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): DataResult<List<T>> {
        val res = primary.findDocuments(collection, filters, clazz)
        return if (res is DataResult.Success) res else secondary.findDocuments(collection, filters, clazz)
    }

    override fun <T : Any> listenToDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Flow<DataResult<T?>> = primary.listenToDocument(collection, documentId, clazz)

    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): Flow<DataResult<List<T>>> = primary.listenToCollection(collection, filters, clazz)
}

/**
 * Implementação híbrida de [StorageRepository] com failover ativo-passivo entre dois provedores de Storage.
 */
internal class HybridStorageRepository(
    private val primary: StorageRepository,
    private val secondary: StorageRepository
) : StorageRepository {

    override fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>> =
        primary.uploadFile(path, fileData)

    override fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>> =
        primary.uploadFile(path, fileUri)

    override fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>> =
        primary.uploadFile(path, inputStream)

    override suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri> {
        val res = primary.uploadFileDirect(path, fileData)
        return if (res is DataResult.Success) res else secondary.uploadFileDirect(path, fileData)
    }

    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> {
        val res = primary.uploadFileDirect(path, fileUri)
        return if (res is DataResult.Success) res else secondary.uploadFileDirect(path, fileUri)
    }

    override suspend fun getDownloadUrl(path: String): DataResult<Uri> {
        val res = primary.getDownloadUrl(path)
        return if (res is DataResult.Success) res else secondary.getDownloadUrl(path)
    }

    override suspend fun delete(path: String): DataResult<Unit> {
        val res = primary.delete(path)
        return if (res is DataResult.Success) res else secondary.delete(path)
    }
}
