package br.wgc.omnibackend.supabase.data.repository

import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.model.firestore.OperatorType
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.supabase.utils.SupabaseErrorMapper
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Implementação corporativa do contrato [FirestoreRepository] utilizando o Supabase PostgREST e Realtime.
 *
 * Provê persistência tipada em tabelas relacionais do Supabase, mapeamento dinâmico de predicados
 * de consulta e escuta reativa de alterações em tempo real via [Realtime] Postgres Changes.
 *
 * @property postgrest Módulo [Postgrest] do cliente Supabase para operações REST.
 * @property realtime Módulo [Realtime] do cliente Supabase para WebSockets e eventos reativos.
 * @property gson Serializador Gson injetado ou instanciado para conversão reflexiva de modelos.
 */
class SupabaseDatabaseRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val gson: Gson = Gson()
) : FirestoreRepository {

    /**
     * Adiciona ou cria um novo registro/documento na tabela/coleção informada.
     *
     * @param T Tipo do payload a ser persistido.
     * @param collection Nome da tabela de destino no Supabase.
     * @param data Objeto com os dados a serem salvos.
     * @param customId Identificador customizado opcional. Se nulo, um UUID aleatório é atribuído.
     * @return [DataResult.Success] com o ID gerado ou atribuído ao documento.
     */
    override suspend fun <T : Any> addDocument(
        collection: String,
        data: T,
        customId: String?
    ): DataResult<String> = runCatching {
        val jsonElement = gson.toJsonTree(data).asJsonObject
        val docId = customId
            ?: (if (jsonElement.has("id") && !jsonElement.get("id").isJsonNull) jsonElement.get("id").asString else UUID.randomUUID().toString())

        if (!jsonElement.has("id")) {
            jsonElement.addProperty("id", docId)
        }

        postgrest.from(collection).insert(jsonElement.toString())
        DataResult.Success(docId)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapDatabaseError(it))
    }

    /**
     * Recupera um documento individual a partir de seu ID primário.
     *
     * @param T Tipo para o qual o registro será desserializado.
     * @param collection Nome da tabela no Supabase.
     * @param documentId Identificador do registro.
     * @param clazz Classe de destino para conversão dos dados.
     * @return [DataResult.Success] com a entidade recuperada ou `null` se não existir.
     */
    override suspend fun <T : Any> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): DataResult<T?> = runCatching {
        val result = postgrest.from(collection).select {
            filter {
                eq("id", documentId)
            }
        }
        val jsonArray = JsonParser.parseString(result.data).asJsonArray
        val doc = if (jsonArray.size() > 0) {
            gson.fromJson(jsonArray.get(0), clazz)
        } else {
            null
        }
        DataResult.Success(doc)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapDatabaseError(it))
    }

    /**
     * Atualiza campos parciais de um registro existente pelo seu ID.
     *
     * @param collection Nome da tabela.
     * @param documentId Identificador do registro a alterar.
     * @param data Mapa chave-valor dos campos a serem atualizados.
     */
    override suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>
    ): DataResult<Unit> = runCatching {
        val jsonString = gson.toJson(data)
        postgrest.from(collection).update(jsonString) {
            filter {
                eq("id", documentId)
            }
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapDatabaseError(it))
    }

    /**
     * Remove fisicamente um registro da tabela pelo seu ID.
     *
     * @param collection Nome da tabela.
     * @param documentId Identificador do registro a ser excluído.
     */
    override suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): DataResult<Unit> = runCatching {
        postgrest.from(collection).delete {
            filter {
                eq("id", documentId)
            }
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapDatabaseError(it))
    }

    /**
     * Executa uma consulta filtrada por predicados relacionais dinâmicos.
     *
     * @param T Tipo para o qual os registros serão convertidos.
     * @param collection Nome da tabela.
     * @param filters Lista de filtros [FilterRequest] aplicados.
     * @param clazz Classe de destino para mapeamento dos registros.
     */
    override suspend fun <T : Any> findDocuments(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): DataResult<List<T>> = runCatching {
        val result = postgrest.from(collection).select {
            filter {
                filters.forEach { applyFilter(it) }
            }
        }
        val jsonArray = JsonParser.parseString(result.data).asJsonArray
        val items = jsonArray.map { gson.fromJson(it, clazz) }
        DataResult.Success(items)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapDatabaseError(it))
    }

    /**
     * Observa as modificações de um registro específico em tempo real.
     *
     * @param T Tipo de destino para conversão dos dados.
     * @param collection Nome da tabela.
     * @param documentId Identificador exclusivo do documento.
     * @param clazz Classe de destino para mapeamento.
     */
    override fun <T : Any> listenToDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Flow<DataResult<T?>> = callbackFlow {
        trySend(getDocument(collection, documentId, clazz))

        val channelName = "doc-$collection-$documentId-${UUID.randomUUID()}"
        val channel = realtime.channel(channelName)

        val job = launch {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = collection
            }
            channel.subscribe()
            changeFlow.collect {
                trySend(getDocument(collection, documentId, clazz))
            }
        }

        awaitClose {
            job.cancel()
            launch {
                runCatching { realtime.removeChannel(channel) }
            }
        }
    }

    /**
     * Observa as modificações dos resultados de uma consulta em tempo real.
     *
     * @param T Tipo de destino para conversão dos registros.
     * @param collection Nome da tabela.
     * @param filters Filtros de composição da consulta.
     * @param clazz Classe de destino para mapeamento.
     */
    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): Flow<DataResult<List<T>>> = callbackFlow {
        trySend(findDocuments(collection, filters, clazz))

        val channelName = "col-$collection-${UUID.randomUUID()}"
        val channel = realtime.channel(channelName)

        val job = launch {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = collection
            }
            channel.subscribe()
            changeFlow.collect {
                trySend(findDocuments(collection, filters, clazz))
            }
        }

        awaitClose {
            job.cancel()
            launch {
                runCatching { realtime.removeChannel(channel) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun PostgrestFilterBuilder.applyFilter(filter: FilterRequest) {
        val field = filter.field
        val value = filter.value
        when (filter.operatorType) {
            OperatorType.EQUAL_TO -> eq(field, value)
            OperatorType.NOT_EQUAL_TO -> neq(field, value)
            OperatorType.GREATER_THAN -> gt(field, value)
            OperatorType.GREATER_THAN_OR_EQUAL_TO -> gte(field, value)
            OperatorType.LESS_THAN -> lt(field, value)
            OperatorType.LESS_THAN_OR_EQUAL_TO -> lte(field, value)
            OperatorType.ARRAY_CONTAINS -> contains(field, listOf(value))
            OperatorType.ARRAY_CONTAINS_ANY -> overlaps(field, (value as? List<*>)?.filterNotNull() ?: listOf(value))
            OperatorType.IN -> (value as? List<*>)?.filterNotNull()?.let { isIn(field, it) } ?: eq(field, value)
            OperatorType.NOT_IN -> (value as? List<*>)?.let { filterNot(field, FilterOperator.IN, it) } ?: neq(field, value)
        }
    }
}
