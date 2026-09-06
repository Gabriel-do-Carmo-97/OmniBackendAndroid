package br.wgc.omnibackend.firebase.data.repository

import br.wgc.omnibackend.firebase.data.model.firestore.FilterRequest
import br.wgc.omnibackend.firebase.data.model.firestore.OperatorType
import br.wgc.omnibackend.firebase.domain.repository.FirestoreRepository
import br.wgc.omnibackend.firebase.utils.AppError
import br.wgc.omnibackend.firebase.utils.DataResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreRepository {

    override suspend fun <T : Any> addDocument(
        collection: String,
        data: T,
        customId: String?
    ): DataResult<String> = runCatching {
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

    override suspend fun <T : Any> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): DataResult<T?> = runCatching {
        val snapshot = firestore.collection(collection).document(documentId).get().await()
        DataResult.Success(snapshot.toObject(clazz))
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    override suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>
    ): DataResult<Unit> = runCatching {
        firestore.collection(collection).document(documentId).update(data).await()
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    override suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): DataResult<Unit> = runCatching {
        firestore.collection(collection).document(documentId).delete().await()
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    override suspend fun <T : Any> findDocuments(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): DataResult<List<T>> = runCatching {
        val query = buildQuery(collection, filters)
        val snapshot = query.get().await()
        DataResult.Success(snapshot.toObjects(clazz))
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    override fun <T : Any> listenToDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Flow<DataResult<T?>> = callbackFlow {
        val listener = firestore.collection(collection).document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Failure(mapExceptionToAppError(error)))
                    close()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(DataResult.Success(snapshot.toObject(clazz)))
                } else {
                    trySend(DataResult.Success(null)) // Documento não existe
                }
            }
        awaitClose { listener.remove() }
    }

    override fun <T : Any> listenToCollection(
        collection: String,
        filters: List<FilterRequest>,
        clazz: Class<T>
    ): Flow<DataResult<List<T>>> = callbackFlow {
        val query = buildQuery(collection, filters)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(DataResult.Failure(mapExceptionToAppError(error)))
                close()
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(DataResult.Success(snapshot.toObjects(clazz)))
            }
        }
        awaitClose { listener.remove() }
    }

    private fun buildQuery(collection: String, filters: List<FilterRequest>): Query {
        var query: Query = firestore.collection(collection)
        filters.forEach { filter ->
            val listValue: List<Any> = when (val v = filter.value) {
                is List<*> -> v.filterNotNull()
                is Iterable<*> -> v.filterNotNull()
                is Array<*> -> v.filterNotNull()
                else -> listOf(v)
            }
            query = when (filter.operatorType) {
                OperatorType.EQUAL_TO -> query.whereEqualTo(filter.field, filter.value)
                OperatorType.NOT_EQUAL_TO -> query.whereNotEqualTo(filter.field, filter.value)
                OperatorType.GREATER_THAN -> query.whereGreaterThan(filter.field, filter.value)
                OperatorType.LESS_THAN -> query.whereLessThan(filter.field, filter.value)
                OperatorType.GREATER_THAN_OR_EQUAL_TO -> query.whereGreaterThanOrEqualTo(filter.field, filter.value)
                OperatorType.LESS_THAN_OR_EQUAL_TO -> query.whereLessThanOrEqualTo(filter.field, filter.value)
                OperatorType.ARRAY_CONTAINS -> query.whereArrayContains(filter.field, filter.value)
                OperatorType.ARRAY_CONTAINS_ANY -> query.whereArrayContainsAny(filter.field, listValue)
                OperatorType.IN -> query.whereIn(filter.field, listValue)
                OperatorType.NOT_IN -> query.whereNotIn(filter.field, listValue)
            }
        }
        return query
    }

    private fun mapExceptionToAppError(exception: Throwable): AppError {
        return when (exception) {
            is FirebaseFirestoreException -> when (exception.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> AppError.Firestore.PermissionDenied
                FirebaseFirestoreException.Code.NOT_FOUND -> AppError.Firestore.DocumentNotFound
                FirebaseFirestoreException.Code.ABORTED -> AppError.Firestore.Aborted
                FirebaseFirestoreException.Code.UNAVAILABLE -> AppError.Generic.Network
                else -> AppError.Firestore.Generic(exception)
            }
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
    }
}

