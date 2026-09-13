package br.wgc.omnibackend.appwrite.data.repository

import br.wgc.omnibackend.appwrite.utils.AppwriteErrorMapper
import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.model.firestore.OperatorType
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.RealtimeResponseEvent
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Implementação concreta de [FirestoreRepository] utilizando o serviço [Databases] do Appwrite SDK.
 *
 * Toda persistência de documentos ocorre dentro do banco de dados `databaseId` e coleção
 * especificada pelo parâmetro `collection` em cada operação.
 *
 * A desserialização de documentos usa [Gson] para converter o `Map<String, Any>` do Appwrite
 * no tipo de destino genérico `T`.
 *
 * @param databases Serviço Databases do Appwrite SDK.
 * @param realtime Serviço Realtime para observação de mudanças em tempo real.
 * @param databaseId Identificador do banco de dados Appwrite a ser utilizado.
 * @param gson Instância de [Gson] para serialização/desserialização de documentos.
 */
internal class AppwriteDatabaseRepositoryImpl(
    private val databases: Databases,
    private val realtime: Realtime,
    private val databaseId: String,
    private val gson: Gson = Gson(),
) : FirestoreRepository {

    // ─── Add Document ─────────────────────────────────────────────────────────

    /**
     * Cria um novo documento na coleção informada.
     *
     * @param T Tipo do payload a ser persistido.
     * @param collection Nome da coleção.
     * @param data Objeto a ser convertido em mapa de dados.
     * @param customId ID personalizado opcional; se `null`, usa [ID.unique].
     * @return [DataResult.Success] com o ID do documento criado.
     */
    override suspend fun <T : Any> addDocument(collection: String, data: T, customId: String?): DataResult<String> = runCatchingDb {
        val docId = customId ?: ID.unique()
        val dataMap = gson.fromJson<Map<String, Any>>(gson.toJson(data), Map::class.java)
        val document = databases.createDocument(databaseId, collection, docId, dataMap)
        document.id
    }

    // ─── Get Document ─────────────────────────────────────────────────────────

    /**
     * Recupera um documento por seu ID, desserializando para o tipo [T].
     *
     * @param T Tipo de destino para mapeamento.
     * @param collection Nome da coleção.
     * @param documentId ID do documento.
     * @param clazz Classe de destino para conversão.
     * @return [DataResult.Success] com o objeto ou `null` se não encontrado.
     */
    override suspend fun <T : Any> getDocument(collection: String, documentId: String, clazz: Class<T>): DataResult<T?> = runCatchingDb {
        val document = databases.getDocument(databaseId, collection, documentId)
        documentToObject(document.data, clazz)
    }

    // ─── Update Document ──────────────────────────────────────────────────────

    /**
     * Atualiza campos pontuais de um documento existente.
     *
     * @param collection Nome da coleção.
     * @param documentId ID do documento a ser alterado.
     * @param data Mapa com os campos e novos valores.
     */
    override suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): DataResult<Unit> = runCatchingDb {
        databases.updateDocument(databaseId, collection, documentId, data)
        Unit
    }

    // ─── Delete Document ──────────────────────────────────────────────────────

    /**
     * Remove permanentemente um documento.
     *
     * @param collection Nome da coleção.
     * @param documentId ID do documento a ser excluído.
     */
    override suspend fun deleteDocument(collection: String, documentId: String): DataResult<Unit> = runCatchingDb {
        databases.deleteDocument(databaseId, collection, documentId)
        Unit
    }

    // ─── Find Documents ───────────────────────────────────────────────────────

    /**
     * Executa uma consulta com filtros e retorna os documentos convertidos para [T].
     *
     * @param T Tipo de destino para mapeamento dos documentos.
     * @param collection Nome da coleção.
     * @param filters Lista de [FilterRequest] a serem aplicados.
     * @param clazz Classe de destino para conversão.
     */
    override suspend fun <T : Any> findDocuments(collection: String, filters: List<FilterRequest>, clazz: Class<T>): DataResult<List<T>> =
        runCatchingDb {
            val queries = filters.map { filter -> filter.toAppwriteQuery() }
            val result = databases.listDocuments(databaseId, collection, queries)
            result.documents.mapNotNull { doc -> documentToObject(doc.data, clazz) }
        }

    // ─── Listen To Document ───────────────────────────────────────────────────

    /**
     * Observa mudanças em tempo real de um documento via Appwrite Realtime.
     *
     * @param T Tipo de destino para conversão dos dados.
     * @param collection Nome da coleção.
     * @param documentId ID exclusivo do documento monitorado.
     * @param clazz Classe de destino para mapeamento.
     * @return [Flow] que emite atualizações contínuas do documento.
     */
    override fun <T : Any> listenToDocument(collection: String, documentId: String, clazz: Class<T>): Flow<DataResult<T?>> = callbackFlow {
        val channel = "databases.$databaseId.collections.$collection.documents.$documentId"
        val subscription = realtime.subscribe(channel) { event: RealtimeResponseEvent<Any> ->
            try {
                @Suppress("UNCHECKED_CAST")
                val dataMap = when (val payload = event.payload) {
                    is Map<*, *> -> payload as Map<String, Any>
                    is io.appwrite.models.Document<*> -> payload.data as? Map<String, Any>
                    else -> null
                }
                val obj = dataMap?.let { documentToObject(it, clazz) }
                trySend(DataResult.Success(obj))
            } catch (e: Exception) {
                trySend(DataResult.Failure(AppwriteErrorMapper.mapThrowable(e, "firestore")))
            }
        }
        // Emit current document state immediately
        try {
            val document = databases.getDocument(databaseId, collection, documentId)
            trySend(DataResult.Success(documentToObject(document.data, clazz)))
        } catch (e: AppwriteException) {
            trySend(DataResult.Failure(AppwriteErrorMapper.mapException(e, "firestore")))
        }
        awaitClose { subscription.close() }
    }

    // ─── Listen To Collection ─────────────────────────────────────────────────

    /**
     * Observa mudanças em tempo real de uma coleção (todos os documentos que correspondem
     * aos filtros são re-consultados a cada evento).
     *
     * @param T Tipo de destino para conversão dos documentos.
     * @param collection Nome da coleção.
     * @param filters Filtros de composição da consulta.
     * @param clazz Classe de destino para mapeamento.
     * @return [Flow] que emite listas atualizadas de registros.
     */
    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>,
    ): Flow<DataResult<List<T>>> = callbackFlow {
        val channel = "databases.$databaseId.collections.$collection.documents"
        val queries = filters.map { it.toAppwriteQuery() }
        val subscription = realtime.subscribe(channel) { _: RealtimeResponseEvent<Any> ->
            launch {
                try {
                    val result = databases.listDocuments(databaseId, collection, queries)
                    val list = result.documents.mapNotNull { doc -> documentToObject(doc.data, clazz) }
                    trySend(DataResult.Success(list))
                } catch (e: AppwriteException) {
                    trySend(DataResult.Failure(AppwriteErrorMapper.mapException(e, "firestore")))
                }
            }
        }
        // Emit initial state
        try {
            val result = databases.listDocuments(databaseId, collection, queries)
            val list = result.documents.mapNotNull { doc -> documentToObject(doc.data, clazz) }
            trySend(DataResult.Success(list))
        } catch (e: AppwriteException) {
            trySend(DataResult.Failure(AppwriteErrorMapper.mapException(e, "firestore")))
        }
        awaitClose { subscription.close() }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun <T : Any> documentToObject(data: Map<String, Any>, clazz: Class<T>): T? = try {
        gson.fromJson(gson.toJson(data), clazz)
    } catch (e: Exception) {
        null
    }

    @Suppress("ComplexMethod")
    private fun FilterRequest.toAppwriteQuery(): String = when (operatorType) {
        OperatorType.EQUAL_TO -> Query.equal(field, value)
        OperatorType.NOT_EQUAL_TO -> Query.notEqual(field, value)
        OperatorType.GREATER_THAN -> Query.greaterThan(field, value)
        OperatorType.LESS_THAN -> Query.lessThan(field, value)
        OperatorType.GREATER_THAN_OR_EQUAL_TO -> Query.greaterThanEqual(field, value)
        OperatorType.LESS_THAN_OR_EQUAL_TO -> Query.lessThanEqual(field, value)
        OperatorType.ARRAY_CONTAINS -> Query.contains(field, value)
        OperatorType.ARRAY_CONTAINS_ANY -> {
            @Suppress("UNCHECKED_CAST")
            Query.containsAny(field, listOf(value) as List<Any>)
        }
        OperatorType.IN -> {
            @Suppress("UNCHECKED_CAST")
            val list = value as? List<Any> ?: listOf(value)
            Query.equal(field, list.firstOrNull() ?: "")
        }
        OperatorType.NOT_IN -> Query.notEqual(field, value)
    }

    private suspend inline fun <T> runCatchingDb(crossinline block: suspend () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: AppwriteException) {
        DataResult.Failure(AppwriteErrorMapper.mapException(e, "firestore"))
    } catch (e: Exception) {
        DataResult.Failure(AppwriteErrorMapper.mapThrowable(e, "firestore"))
    }
}
