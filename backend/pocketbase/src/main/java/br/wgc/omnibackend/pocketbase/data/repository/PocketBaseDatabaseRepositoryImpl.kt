package br.wgc.omnibackend.pocketbase.data.repository

import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.model.firestore.OperatorType
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.pocketbase.utils.PocketBaseErrorMapper
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Implementação de [FirestoreRepository] utilizando a API de Collections do PocketBase (`/api/collections/{collection}/records`).
 */
internal class PocketBaseDatabaseRepositoryImpl(
    private val baseUrl: String,
    private val gson: Gson = Gson()
) : FirestoreRepository {

    override suspend fun <T : Any> addDocument(
        collection: String,
        data: T,
        customId: String?
    ): DataResult<String> = runCatchingDb {
        val url = "$baseUrl/api/collections/$collection/records"
        val dataMap = gson.fromJson<Map<String, Any>>(gson.toJson(data), Map::class.java).toMutableMap()
        if (customId != null) {
            dataMap["id"] = customId
        }
        val response = httpPost(url, dataMap)
        response["id"]?.toString() ?: throw IllegalStateException("PocketBase didn't return record id")
    }

    override suspend fun <T : Any> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): DataResult<T?> = runCatchingDb {
        val url = "$baseUrl/api/collections/$collection/records/$documentId"
        val response = httpGet(url)
        gson.fromJson(gson.toJson(response), clazz)
    }

    override suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>
    ): DataResult<Unit> = runCatchingDb {
        val url = "$baseUrl/api/collections/$collection/records/$documentId"
        httpPatch(url, data)
        Unit
    }

    override suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): DataResult<Unit> = runCatchingDb {
        val url = "$baseUrl/api/collections/$collection/records/$documentId"
        httpDelete(url)
        Unit
    }

    override suspend fun <T : Any> findDocuments(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): DataResult<List<T>> = runCatchingDb {
        val filterQuery = buildFilterQuery(filters)
        val encodedFilter = if (filterQuery.isNotBlank()) "?filter=" + URLEncoder.encode(filterQuery, "UTF-8") else ""
        val url = "$baseUrl/api/collections/$collection/records$encodedFilter"
        val response = httpGet(url)
        @Suppress("UNCHECKED_CAST")
        val items = response["items"] as? List<Map<String, Any?>> ?: emptyList()
        items.mapNotNull { item ->
            try {
                gson.fromJson(gson.toJson(item), clazz)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun <T : Any> listenToDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Flow<DataResult<T?>> = callbackFlow {
        try {
            val docResult = getDocument(collection, documentId, clazz)
            trySend(docResult)
        } catch (e: Exception) {
            trySend(DataResult.Failure(PocketBaseErrorMapper.mapThrowable(e, "firestore")))
        }
        awaitClose()
    }

    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): Flow<DataResult<List<T>>> = callbackFlow {
        try {
            val listResult = findDocuments(collection, filters, clazz)
            trySend(listResult)
        } catch (e: Exception) {
            trySend(DataResult.Failure(PocketBaseErrorMapper.mapThrowable(e, "firestore")))
        }
        awaitClose()
    }

    private fun buildFilterQuery(filters: List<FilterRequest>): String {
        return filters.joinToString(" && ") { filter ->
            val field = filter.field
            val value = filter.value
            when (filter.operatorType) {
                OperatorType.EQUAL_TO -> "$field = '$value'"
                OperatorType.NOT_EQUAL_TO -> "$field != '$value'"
                OperatorType.GREATER_THAN -> "$field > '$value'"
                OperatorType.LESS_THAN -> "$field < '$value'"
                OperatorType.GREATER_THAN_OR_EQUAL_TO -> "$field >= '$value'"
                OperatorType.LESS_THAN_OR_EQUAL_TO -> "$field <= '$value'"
                OperatorType.ARRAY_CONTAINS -> "$field ~ '$value'"
                OperatorType.ARRAY_CONTAINS_ANY -> "$field ~ '$value'"
                OperatorType.IN -> "$field = '$value'"
                OperatorType.NOT_IN -> "$field != '$value'"
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun httpGet(urlString: String): Map<String, Any?> {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
        }
        val text = conn.inputStream.use { it.bufferedReader().readText() }
        return gson.fromJson(text, Map::class.java) as Map<String, Any?>
    }

    @Suppress("UNCHECKED_CAST")
    private fun httpPost(urlString: String, data: Map<String, Any?>): Map<String, Any?> {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }
        val json = gson.toJson(data)
        conn.outputStream.use { it.write(json.toByteArray()) }
        val text = conn.inputStream.use { it.bufferedReader().readText() }
        return gson.fromJson(text, Map::class.java) as Map<String, Any?>
    }

    @Suppress("UNCHECKED_CAST")
    private fun httpPatch(urlString: String, data: Map<String, Any?>): Map<String, Any?> {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "PATCH"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }
        val json = gson.toJson(data)
        conn.outputStream.use { it.write(json.toByteArray()) }
        val text = conn.inputStream.use { it.bufferedReader().readText() }
        return gson.fromJson(text, Map::class.java) as Map<String, Any?>
    }

    private fun httpDelete(urlString: String) {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 10000
            readTimeout = 10000
        }
        conn.responseCode
    }

    private inline fun <T> runCatchingDb(block: () -> T): DataResult<T> {
        return try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Failure(PocketBaseErrorMapper.mapThrowable(e, "firestore"))
        }
    }
}
