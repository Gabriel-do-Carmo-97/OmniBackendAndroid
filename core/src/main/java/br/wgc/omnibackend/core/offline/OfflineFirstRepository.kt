package br.wgc.omnibackend.core.offline

import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.network.NetworkMonitor
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Repositório decorador agnóstico que encapsula [FirestoreRepository] e fornece resiliência offline.
 *
 * Mutações executadas enquanto o dispositivo está desconectado são armazenadas em [MutationQueueRepository]
 * e sincronizadas de forma transparente via [syncPendingMutations] assim que o [NetworkMonitor] indica retorno da rede.
 *
 * @param remoteRepository Repositório remoto de destino.
 * @param mutationQueue Fila de mutações pendentes.
 * @param networkMonitor Monitor reativo de conectividade.
 * @param scope Escopo de corrotinas para monitoramento de rede e sync background.
 * @param gson Instância de Gson para serialização de payloads na fila.
 */
class OfflineFirstRepository(
    private val remoteRepository: FirestoreRepository,
    private val mutationQueue: MutationQueueRepository,
    private val networkMonitor: NetworkMonitor,
    private val scope: CoroutineScope,
    private val gson: Gson = Gson()
) : FirestoreRepository {

    private val localCache = ConcurrentHashMap<String, MutableMap<String, Any>>()

    init {
        scope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                if (isOnline) {
                    syncPendingMutations()
                }
            }
        }
    }

    override suspend fun <T : Any> addDocument(
        collection: String,
        data: T,
        customId: String?
    ): DataResult<String> {
        val docId = customId ?: UUID.randomUUID().toString()
        val json = gson.toJson(data)

        // Tenta enviar para o servidor remoto
        val result = remoteRepository.addDocument(collection, data, docId)
        if (result is DataResult.Success) {
            return result
        }

        // Se falhar (ex: offline), grava na fila de mutações
        val mutation = PendingMutation(
            id = UUID.randomUUID().toString(),
            collection = collection,
            documentId = docId,
            type = MutationType.ADD,
            payloadJson = json
        )
        mutationQueue.enqueue(mutation)
        return DataResult.Success(docId)
    }

    override suspend fun <T : Any> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): DataResult<T?> {
        return remoteRepository.getDocument(collection, documentId, clazz)
    }

    override suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>
    ): DataResult<Unit> {
        val json = gson.toJson(data)
        val result = remoteRepository.updateDocument(collection, documentId, data)
        if (result is DataResult.Success) {
            return result
        }

        val mutation = PendingMutation(
            id = UUID.randomUUID().toString(),
            collection = collection,
            documentId = documentId,
            type = MutationType.UPDATE,
            payloadJson = json
        )
        mutationQueue.enqueue(mutation)
        return DataResult.Success(Unit)
    }

    override suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): DataResult<Unit> {
        val result = remoteRepository.deleteDocument(collection, documentId)
        if (result is DataResult.Success) {
            return result
        }

        val mutation = PendingMutation(
            id = UUID.randomUUID().toString(),
            collection = collection,
            documentId = documentId,
            type = MutationType.DELETE,
            payloadJson = ""
        )
        mutationQueue.enqueue(mutation)
        return DataResult.Success(Unit)
    }

    override suspend fun <T : Any> findDocuments(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): DataResult<List<T>> {
        return remoteRepository.findDocuments(collection, filters, clazz)
    }

    override fun <T : Any> listenToDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Flow<DataResult<T?>> {
        return remoteRepository.listenToDocument(collection, documentId, clazz)
    }

    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): Flow<DataResult<List<T>>> {
        return remoteRepository.listenToCollection(collection, filters, clazz)
    }

    /**
     * Sincroniza todas as mutações pendentes enfileiradas com o repositório remoto.
     */
    suspend fun syncPendingMutations() {
        val pending = mutationQueue.getPendingMutations()
        for (mutation in pending) {
            val success = when (mutation.type) {
                MutationType.ADD -> {
                    val map = gson.fromJson(mutation.payloadJson, Map::class.java)
                    if (map != null) {
                        remoteRepository.addDocument(mutation.collection, map as Any, mutation.documentId) is DataResult.Success
                    } else false
                }
                MutationType.UPDATE -> {
                    @Suppress("UNCHECKED_CAST")
                    val map = gson.fromJson(mutation.payloadJson, Map::class.java) as Map<String, Any>
                    remoteRepository.updateDocument(mutation.collection, mutation.documentId, map) is DataResult.Success
                }
                MutationType.DELETE -> {
                    remoteRepository.deleteDocument(mutation.collection, mutation.documentId) is DataResult.Success
                }
            }
            if (success) {
                mutationQueue.remove(mutation.id)
            }
        }
    }
}
