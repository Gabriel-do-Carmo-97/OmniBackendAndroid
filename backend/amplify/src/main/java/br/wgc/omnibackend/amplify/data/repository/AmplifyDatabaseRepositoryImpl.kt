package br.wgc.omnibackend.amplify.data.repository

import br.wgc.omnibackend.amplify.utils.AmplifyErrorMapper
import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementação de [FirestoreRepository] simulando persistência em memória/AppSync para AWS Amplify.
 */
internal class AmplifyDatabaseRepositoryImpl(private val gson: Gson = Gson()) : FirestoreRepository {

    private val storageMap = ConcurrentHashMap<String, MutableMap<String, Any>>()

    override suspend fun <T : Any> addDocument(collection: String, data: T, customId: String?): DataResult<String> = runCatchingDb {
        val docId = customId ?: UUID.randomUUID().toString()
        val dataMap = gson.fromJson<Map<String, Any>>(gson.toJson(data), Map::class.java).toMutableMap()
        dataMap["id"] = docId
        val collectionStore = storageMap.getOrPut(collection) { ConcurrentHashMap() }
        collectionStore[docId] = dataMap
        docId
    }

    override suspend fun <T : Any> getDocument(collection: String, documentId: String, clazz: Class<T>): DataResult<T?> = runCatchingDb {
        val collectionStore = storageMap[collection]
        val dataMap = collectionStore?.get(documentId)
        dataMap?.let { gson.fromJson(gson.toJson(it), clazz) }
    }

    override suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): DataResult<Unit> = runCatchingDb {
        val collectionStore = storageMap[collection]
            ?: return DataResult.Failure(br.wgc.omnibackend.core.utils.AppError.Firestore.DocumentNotFound)

        @Suppress("UNCHECKED_CAST")
        val existing = (collectionStore[documentId] as? MutableMap<String, Any>)
            ?: return DataResult.Failure(br.wgc.omnibackend.core.utils.AppError.Firestore.DocumentNotFound)
        existing.putAll(data)
    }

    override suspend fun deleteDocument(collection: String, documentId: String): DataResult<Unit> = runCatchingDb {
        val collectionStore = storageMap[collection]
        collectionStore?.remove(documentId)
    }

    override suspend fun <T : Any> findDocuments(collection: String, filters: List<FilterRequest>, clazz: Class<T>): DataResult<List<T>> =
        runCatchingDb {
            val collectionStore = storageMap[collection] ?: emptyMap()
            collectionStore.values.mapNotNull {
                try {
                    gson.fromJson(gson.toJson(it), clazz)
                } catch (e: Exception) {
                    null
                }
            }
        }

    override fun <T : Any> listenToDocument(collection: String, documentId: String, clazz: Class<T>): Flow<DataResult<T?>> = callbackFlow {
        try {
            val docResult = getDocument(collection, documentId, clazz)
            trySend(docResult)
        } catch (e: Exception) {
            trySend(DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "firestore")))
        }
        awaitClose()
    }

    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>,
    ): Flow<DataResult<List<T>>> = callbackFlow {
        try {
            val listResult = findDocuments(collection, filters, clazz)
            trySend(listResult)
        } catch (e: Exception) {
            trySend(DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "firestore")))
        }
        awaitClose()
    }

    private inline fun <T> runCatchingDb(block: () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "firestore"))
    }
}
