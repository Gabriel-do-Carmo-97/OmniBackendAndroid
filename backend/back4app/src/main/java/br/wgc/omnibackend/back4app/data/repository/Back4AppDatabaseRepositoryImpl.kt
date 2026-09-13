package br.wgc.omnibackend.back4app.data.repository

import br.wgc.omnibackend.back4app.utils.Back4AppErrorMapper
import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.model.firestore.OperatorType
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementação concreta de [FirestoreRepository] utilizando o Parse SDK ([ParseObject] e [ParseQuery]).
 */
internal class Back4AppDatabaseRepositoryImpl(private val gson: Gson = Gson()) : FirestoreRepository {

    override suspend fun <T : Any> addDocument(collection: String, data: T, customId: String?): DataResult<String> = runCatchingDb {
        val parseObject = ParseObject(collection)
        val jsonMap = gson.fromJson<Map<String, Any>>(gson.toJson(data), Map::class.java)
        jsonMap.forEach { (key, value) ->
            parseObject.put(key, value)
        }
        if (customId != null) {
            parseObject.objectId = customId
        }
        parseObject.save()
        parseObject.objectId
    }

    override suspend fun <T : Any> getDocument(collection: String, documentId: String, clazz: Class<T>): DataResult<T?> = runCatchingDb {
        val query = ParseQuery.getQuery<ParseObject>(collection)
        val obj = query.get(documentId)
        parseObjectToObject(obj, clazz)
    }

    override suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): DataResult<Unit> = runCatchingDb {
        val query = ParseQuery.getQuery<ParseObject>(collection)
        val obj = query.get(documentId)
        data.forEach { (key, value) ->
            obj.put(key, value)
        }
        obj.save()
        Unit
    }

    override suspend fun deleteDocument(collection: String, documentId: String): DataResult<Unit> = runCatchingDb {
        val query = ParseQuery.getQuery<ParseObject>(collection)
        val obj = query.get(documentId)
        obj.delete()
        Unit
    }

    override suspend fun <T : Any> findDocuments(collection: String, filters: List<FilterRequest>, clazz: Class<T>): DataResult<List<T>> =
        runCatchingDb {
            val query = ParseQuery.getQuery<ParseObject>(collection)
            filters.forEach { filter ->
                query.applyFilter(filter)
            }
            val results = query.find()
            results.mapNotNull { parseObjectToObject(it, clazz) }
        }

    override fun <T : Any> listenToDocument(collection: String, documentId: String, clazz: Class<T>): Flow<DataResult<T?>> = callbackFlow {
        try {
            val query = ParseQuery.getQuery<ParseObject>(collection)
            val obj = query.get(documentId)
            trySend(DataResult.Success(parseObjectToObject(obj, clazz)))
        } catch (e: Exception) {
            trySend(DataResult.Failure(Back4AppErrorMapper.mapThrowable(e, "firestore")))
        }
        awaitClose()
    }

    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>,
    ): Flow<DataResult<List<T>>> = callbackFlow {
        try {
            val query = ParseQuery.getQuery<ParseObject>(collection)
            filters.forEach { query.applyFilter(it) }
            val results = query.find()
            trySend(DataResult.Success(results.mapNotNull { parseObjectToObject(it, clazz) }))
        } catch (e: Exception) {
            trySend(DataResult.Failure(Back4AppErrorMapper.mapThrowable(e, "firestore")))
        }
        awaitClose()
    }

    private fun <T : Any> parseObjectToObject(parseObject: ParseObject, clazz: Class<T>): T? = try {
        val map = mutableMapOf<String, Any?>()
        map["id"] = parseObject.objectId
        parseObject.keySet().forEach { key ->
            map[key] = parseObject.get(key)
        }
        gson.fromJson(gson.toJson(map), clazz)
    } catch (e: Exception) {
        null
    }

    private fun ParseQuery<ParseObject>.applyFilter(filter: FilterRequest) {
        when (filter.operatorType) {
            OperatorType.EQUAL_TO -> whereEqualTo(filter.field, filter.value)
            OperatorType.NOT_EQUAL_TO -> whereNotEqualTo(filter.field, filter.value)
            OperatorType.GREATER_THAN -> whereGreaterThan(filter.field, filter.value)
            OperatorType.LESS_THAN -> whereLessThan(filter.field, filter.value)
            OperatorType.GREATER_THAN_OR_EQUAL_TO -> whereGreaterThanOrEqualTo(filter.field, filter.value)
            OperatorType.LESS_THAN_OR_EQUAL_TO -> whereLessThanOrEqualTo(filter.field, filter.value)
            OperatorType.ARRAY_CONTAINS -> whereEqualTo(filter.field, filter.value)
            OperatorType.ARRAY_CONTAINS_ANY -> {
                val list = filter.value as? List<*> ?: listOf(filter.value)
                whereContainedIn(filter.field, list)
            }
            OperatorType.IN -> {
                val list = filter.value as? List<*> ?: listOf(filter.value)
                whereContainedIn(filter.field, list)
            }
            OperatorType.NOT_IN -> {
                val list = filter.value as? List<*> ?: listOf(filter.value)
                whereNotContainedIn(filter.field, list)
            }
        }
    }

    private inline fun <T> runCatchingDb(block: () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: ParseException) {
        DataResult.Failure(Back4AppErrorMapper.mapException(e, "firestore"))
    } catch (e: Exception) {
        DataResult.Failure(Back4AppErrorMapper.mapThrowable(e, "firestore"))
    }
}
