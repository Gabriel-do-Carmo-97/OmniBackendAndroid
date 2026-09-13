package br.wgc.omnibackend.firebase.data.repository

import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.model.firestore.OperatorType
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

/**
 * Implementação do contrato [FirestoreRepository] utilizando o Google Cloud Firestore SDK.
 *
 * Provê suporte a persistência NoSQL em coleções, queries dinâmicas compostas e escuta reativa
 * de alterações em tempo real via [callbackFlow].
 *
 * @property firestore Instância do [FirebaseFirestore] injetada.
 */
class FirestoreRepositoryImpl @Inject constructor(private val firestore: FirebaseFirestore) : FirestoreRepository {

    /**
     * Adiciona ou cria um novo documento no Cloud Firestore.
     *
     * @param T Tipo da entidade a ser serializada pelo Firestore.
     * @param collection Nome da coleção.
     * @param data Objeto a ser persistido.
     * @param customId Identificador opcional. Se nulo, o Firestore gera um hash alfanumérico automático.
     * @return [DataResult.Success] contendo o ID do documento.
     */
    override suspend fun <T : Any> addDocument(collection: String, data: T, customId: String?): DataResult<String> = runCatching {
        val documentReference = if (customId != null) {
            firestore.collection(collection).document(customId)
        } else {
            firestore.collection(collection).document()
        }
        documentReference.set(data).await()
        DataResult.Success(documentReference.id)
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    /**
     * Busca um documento individual a partir do seu ID.
     *
     * @param T Tipo para o qual o documento será convertido.
     * @param collection Nome da coleção.
     * @param documentId Identificador do documento.
     * @param clazz Classe de destino para a desserialização.
     * @return [DataResult.Success] com o objeto recuperado ou `null` se não existir.
     */
    override suspend fun <T : Any> getDocument(collection: String, documentId: String, clazz: Class<T>): DataResult<T?> = runCatching {
        val snapshot = firestore.collection(collection).document(documentId).get().await()
        DataResult.Success(snapshot.toObject(clazz))
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    /**
     * Atualiza atributos específicos de um documento existente.
     *
     * @param collection Nome da coleção.
     * @param documentId Identificador do documento.
     * @param data Mapa de pares campo-valor a serem alterados.
     */
    override suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): DataResult<Unit> = runCatching {
        firestore.collection(collection).document(documentId).update(data).await()
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    /**
     * Deleta um documento do Firestore.
     *
     * @param collection Nome da coleção.
     * @param documentId Identificador do documento a excluir.
     */
    override suspend fun deleteDocument(collection: String, documentId: String): DataResult<Unit> = runCatching {
        firestore.collection(collection).document(documentId).delete().await()
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    /**
     * Executa uma consulta filtrada por múltiplos predicados relacionais.
     *
     * @param T Tipo de destino para a conversão dos registros.
     * @param collection Nome da coleção.
     * @param filters Lista de filtros [FilterRequest].
     * @param clazz Classe de destino para mapeamento reflexivo.
     * @return [DataResult.Success] contendo a lista dos objetos correspondentes.
     */
    override suspend fun <T : Any> findDocuments(collection: String, filters: List<FilterRequest>, clazz: Class<T>): DataResult<List<T>> =
        runCatching {
            var query: Query = firestore.collection(collection)
            filters.forEach { filter ->
                query = applyFilter(query, filter)
            }
            val querySnapshot = query.get().await()
            val resultList = querySnapshot.documents.mapNotNull { it.toObject(clazz) }
            DataResult.Success(resultList)
        }.getOrElse {
            DataResult.Failure(mapExceptionToAppError(it))
        }

    /**
     * Escuta atualizações de um documento em tempo real via snapshot listener.
     *
     * @param T Tipo de destino para conversão.
     * @param collection Nome da coleção.
     * @param documentId Identificador do documento.
     * @param clazz Classe de destino.
     * @return [Flow] que emite atualizações contínuas do documento.
     */
    override fun <T : Any> listenToDocument(collection: String, documentId: String, clazz: Class<T>): Flow<DataResult<T?>> = callbackFlow {
        val listenerRegistration = firestore.collection(collection).document(documentId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    trySend(DataResult.Failure(mapExceptionToAppError(exception)))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(DataResult.Success(snapshot.toObject(clazz)))
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Escuta os resultados de uma consulta em tempo real via snapshot listener.
     *
     * @param T Tipo de destino dos registros.
     * @param collection Nome da coleção.
     * @param filters Lista de filtros a aplicar à consulta.
     * @param clazz Classe de destino.
     * @return [Flow] emitindo listas atualizadas de registros.
     */
    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>,
    ): Flow<DataResult<List<T>>> = callbackFlow {
        var query: Query = firestore.collection(collection)
        filters.forEach { filter ->
            query = applyFilter(query, filter)
        }

        val listenerRegistration = query.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                trySend(DataResult.Failure(mapExceptionToAppError(exception)))
                return@addSnapshotListener
            }
            if (querySnapshot != null) {
                val resultList = querySnapshot.documents.mapNotNull { it.toObject(clazz) }
                trySend(DataResult.Success(resultList))
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    private fun applyFilter(query: Query, filter: FilterRequest): Query = when (filter.operatorType) {
        OperatorType.EQUAL_TO -> query.whereEqualTo(filter.field, filter.value)
        OperatorType.NOT_EQUAL_TO -> query.whereNotEqualTo(filter.field, filter.value)
        OperatorType.GREATER_THAN -> query.whereGreaterThan(filter.field, filter.value)
        OperatorType.LESS_THAN -> query.whereLessThan(filter.field, filter.value)
        OperatorType.GREATER_THAN_OR_EQUAL_TO -> query.whereGreaterThanOrEqualTo(filter.field, filter.value)
        OperatorType.LESS_THAN_OR_EQUAL_TO -> query.whereLessThanOrEqualTo(filter.field, filter.value)
        OperatorType.ARRAY_CONTAINS -> query.whereArrayContains(filter.field, filter.value)
        OperatorType.ARRAY_CONTAINS_ANY -> (filter.value as? List<*>)?.let { query.whereArrayContainsAny(filter.field, it) } ?: query
        OperatorType.IN -> (filter.value as? List<*>)?.let { query.whereIn(filter.field, it) } ?: query
        OperatorType.NOT_IN -> (filter.value as? List<*>)?.let { query.whereNotIn(filter.field, it) } ?: query
    }

    private fun mapExceptionToAppError(throwable: Throwable): AppError = when (throwable) {
        is FirebaseFirestoreException -> when (throwable.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> AppError.Firestore.PermissionDenied
            FirebaseFirestoreException.Code.NOT_FOUND -> AppError.Firestore.DocumentNotFound
            FirebaseFirestoreException.Code.ABORTED -> AppError.Firestore.Aborted
            else -> AppError.Firestore.Generic(throwable)
        }
        is IOException -> AppError.Generic.Network
        is Exception -> AppError.Generic.Unknown(throwable)
        else -> AppError.Generic.Unknown(throwable)
    }
}
