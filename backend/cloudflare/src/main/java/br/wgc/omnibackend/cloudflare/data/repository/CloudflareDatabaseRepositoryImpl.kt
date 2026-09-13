package br.wgc.omnibackend.cloudflare.data.repository

import br.wgc.omnibackend.cloudflare.utils.CloudflareErrorMapper
import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.HttpURLConnection
import java.net.URL

/**
 * Implementação de [FirestoreRepository] integrando com Cloudflare D1 via Worker REST API.
 */
internal class CloudflareDatabaseRepositoryImpl(private val workerBaseUrl: String, private val gson: Gson = Gson()) : FirestoreRepository {

    override suspend fun <T : Any> addDocument(collection: String, data: T, customId: String?): DataResult<String> = runCatchingDb {
        val url = "$workerBaseUrl/d1/$collection"
        val dataMap = gson.fromJson<Map<String, Any>>(gson.toJson(data), Map::class.java).toMutableMap()
        if (customId != null) {
            dataMap["id"] = customId
        }
        val response = httpPost(url, dataMap)
        response["id"]?.toString() ?: throw IllegalStateException("Cloudflare D1 did not return document id")
    }

    override suspend fun <T : Any> getDocument(collection: String, documentId: String, clazz: Class<T>): DataResult<T?> = runCatchingDb {
        val url = "$workerBaseUrl/d1/$collection/$documentId"
        val response = httpGet(url)
        gson.fromJson(gson.toJson(response), clazz)
    }

    override suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): DataResult<Unit> = runCatchingDb {
        val url = "$workerBaseUrl/d1/$collection/$documentId"
        httpPut(url, data)
        Unit
    }

    override suspend fun deleteDocument(collection: String, documentId: String): DataResult<Unit> = runCatchingDb {
        val url = "$workerBaseUrl/d1/$collection/$documentId"
        httpDelete(url)
        Unit
    }

    override suspend fun <T : Any> findDocuments(collection: String, filters: List<FilterRequest>, clazz: Class<T>): DataResult<List<T>> =
        runCatchingDb {
            val url = "$workerBaseUrl/d1/$collection"
            val response = httpPost("$url/query", mapOf("filters" to filters))

            @Suppress("UNCHECKED_CAST")
            val items = response["results"] as? List<Map<String, Any?>> ?: emptyList()
            items.mapNotNull { item ->
                try {
                    gson.fromJson(gson.toJson(item), clazz)
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
            trySend(DataResult.Failure(CloudflareErrorMapper.mapThrowable(e, "firestore")))
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
            trySend(DataResult.Failure(CloudflareErrorMapper.mapThrowable(e, "firestore")))
        }
        awaitClose()
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
    private fun httpPut(urlString: String, data: Map<String, Any?>): Map<String, Any?> {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
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

    private inline fun <T> runCatchingDb(block: () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Failure(CloudflareErrorMapper.mapThrowable(e, "firestore"))
    }
}
