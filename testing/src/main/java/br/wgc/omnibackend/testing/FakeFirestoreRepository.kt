package br.wgc.omnibackend.testing

import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Implementação em memória de [FirestoreRepository] para testes unitários.
 * Permite simular operações completas de CRUD, buscas e observabilidade NoSQL.
 */
class FakeFirestoreRepository(private val gson: Gson = Gson()) : FirestoreRepository {

    private val collections = mutableMapOf<String, MutableMap<String, String>>()
    private val updateTriggers = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Permite injetar um erro simulado para testar fluxos de falha. */
    var simulatedError: AppError? = null

    override suspend fun <T : Any> addDocument(collection: String, data: T, customId: String?): DataResult<String> {
        simulatedError?.let { return DataResult.Failure(it) }
        val id = customId ?: UUID.randomUUID().toString()
        val docMap = collections.getOrPut(collection) { mutableMapOf() }
        val json = gson.toJson(data)
        docMap[id] = json
        updateTriggers.tryEmit(Unit)
        return DataResult.Success(id)
    }

    override suspend fun <T : Any> getDocument(collection: String, documentId: String, clazz: Class<T>): DataResult<T?> {
        simulatedError?.let { return DataResult.Failure(it) }
        val json = collections[collection]?.get(documentId)
        return if (json != null) {
            val parsed = gson.fromJson(json, clazz)
            DataResult.Success(parsed)
        } else {
            DataResult.Success(null)
        }
    }

    override suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        val existingJson = collections[collection]?.get(documentId)
            ?: return DataResult.Failure(AppError.Firestore.DocumentNotFound)

        @Suppress("UNCHECKED_CAST")
        val currentMap = gson.fromJson(existingJson, MutableMap::class.java) as MutableMap<String, Any>
        currentMap.putAll(data)
        collections[collection]?.put(documentId, gson.toJson(currentMap))
        updateTriggers.tryEmit(Unit)
        return DataResult.Success(Unit)
    }

    override suspend fun deleteDocument(collection: String, documentId: String): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        collections[collection]?.remove(documentId)
        updateTriggers.tryEmit(Unit)
        return DataResult.Success(Unit)
    }

    override suspend fun <T : Any> findDocuments(collection: String, filters: List<FilterRequest>, clazz: Class<T>): DataResult<List<T>> {
        simulatedError?.let { return DataResult.Failure(it) }
        val allDocs = collections[collection]?.values.orEmpty()
        val list: List<T> = allDocs.map { json -> gson.fromJson(json, clazz) }
        return DataResult.Success(list)
    }

    override fun <T : Any> listenToDocument(collection: String, documentId: String, clazz: Class<T>): Flow<DataResult<T?>> = flow {
        emit(getDocument(collection, documentId, clazz))
        updateTriggers.collect {
            emit(getDocument(collection, documentId, clazz))
        }
    }

    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>,
    ): Flow<DataResult<List<T>>> = flow {
        emit(findDocuments(collection, filters, clazz))
        updateTriggers.collect {
            emit(findDocuments(collection, filters, clazz))
        }
    }
}
