package br.wgc.omnibackend.cloudflare.data.repository

import br.wgc.omnibackend.cloudflare.utils.CloudflareErrorMapper
import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.network.OmniHttpClientFactory
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Implementação de [FirestoreRepository] integrando com Cloudflare D1 via Worker REST API.
 */
internal class CloudflareDatabaseRepositoryImpl(
    private val workerBaseUrl: String,
    private val gson: Gson = Gson(),
    private val httpClient: OkHttpClient = OmniHttpClientFactory.createClient(),
) : FirestoreRepository {

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
        val request = Request.Builder().url(urlString).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val text = response.body?.string().orEmpty()
                return gson.fromJson(text, Map::class.java) as Map<String, Any?>
            } else {
                throw IllegalStateException("Cloudflare D1 HTTP ${response.code}: ${response.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun httpPost(urlString: String, data: Map<String, Any?>): Map<String, Any?> {
        val json = gson.toJson(data)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(urlString).post(body).build()
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val text = response.body?.string().orEmpty()
                return gson.fromJson(text, Map::class.java) as Map<String, Any?>
            } else {
                throw IllegalStateException("Cloudflare D1 HTTP ${response.code}: ${response.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun httpPut(urlString: String, data: Map<String, Any?>): Map<String, Any?> {
        val json = gson.toJson(data)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(urlString).put(body).build()
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val text = response.body?.string().orEmpty()
                return gson.fromJson(text, Map::class.java) as Map<String, Any?>
            } else {
                throw IllegalStateException("Cloudflare D1 HTTP ${response.code}: ${response.message}")
            }
        }
    }

    private fun httpDelete(urlString: String) {
        val request = Request.Builder().url(urlString).delete().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Cloudflare D1 HTTP ${response.code}: ${response.message}")
            }
        }
    }

    private inline fun <T> runCatchingDb(block: () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Failure(CloudflareErrorMapper.mapThrowable(e, "firestore"))
    }
}
