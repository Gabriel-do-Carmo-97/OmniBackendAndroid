package br.wgc.omnibackend.firebase.data.repository.realtime

import br.wgc.omnibackend.core.model.database.geo.LocationRequest
import br.wgc.omnibackend.core.repository.realtime.GeolocationRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

/**
 * Implementação do contrato [GeolocationRepository] sobre o Firebase Realtime Database.
 *
 * @property database Instância do [FirebaseDatabase] configurada.
 */
class GeolocationRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
) : GeolocationRepository {

    private val locationsRef = database.getReference("locations")

    /**
     * Atualiza as coordenadas geográficas de uma entidade sob o nó /locations/{entityType}/{entityId}.
     *
     * @param entityType Tipo da entidade (ex: "users", "drivers").
     * @param entityId ID exclusivo da entidade.
     * @param location Modelo com latitude, longitude e timestamp.
     * @return [DataResult.Success] com mensagem de confirmação.
     */
    override suspend fun updateLocation(
        entityType: String,
        entityId: String,
        location: LocationRequest
    ): DataResult<String> = runCatching {
        locationsRef.child(entityType)
            .child(entityId)
            .setValue(location)
            .await()
        DataResult.Success("Localizacao atualizada")
    }.getOrElse { exception ->
        val appError = when (exception) {
            is DatabaseException -> {
                when {
                    exception.message?.contains("permission_denied", ignoreCase = true) == true ->
                        AppError.RealtimeDatabase.PermissionDenied

                    exception.message?.contains("disconnected", ignoreCase = true) == true ->
                        AppError.RealtimeDatabase.Disconnected

                    exception.message?.contains("expired_token", ignoreCase = true) == true ->
                        AppError.RealtimeDatabase.ExpiredToken

                    exception.message?.contains("invalid_token", ignoreCase = true) == true ->
                        AppError.RealtimeDatabase.InvalidToken

                    exception.message?.contains("unavailable", ignoreCase = true) == true ->
                        AppError.RealtimeDatabase.Unavailable

                    exception.message?.contains("write_canceled", ignoreCase = true) == true ->
                        AppError.RealtimeDatabase.WriteCanceled

                    else -> AppError.RealtimeDatabase.OperationFailed
                }
            }

            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(appError)
    }

    /**
     * Observa a localização contínua de uma entidade em tempo real.
     *
     * @param entityType Categoria da entidade.
     * @param entityId ID da entidade.
     * @return [Flow] emitindo o [LocationRequest] sempre que a posição mudar.
     */
    override fun trackLocation(entityType: String, entityId: String): Flow<DataResult<LocationRequest>> =
        callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<LocationRequest>()?.let {
                        trySend(DataResult.Success(it))
                    } ?: trySend(DataResult.Failure(AppError.RealtimeDatabase.OperationFailed))
                }

                override fun onCancelled(error: DatabaseError) {
                    val appError = when (error.code) {
                        DatabaseError.PERMISSION_DENIED -> AppError.RealtimeDatabase.PermissionDenied
                        DatabaseError.DATA_STALE -> AppError.RealtimeDatabase.DataStale
                        DatabaseError.DISCONNECTED -> AppError.RealtimeDatabase.Disconnected
                        DatabaseError.EXPIRED_TOKEN -> AppError.RealtimeDatabase.ExpiredToken
                        DatabaseError.INVALID_TOKEN -> AppError.RealtimeDatabase.InvalidToken
                        DatabaseError.MAX_RETRIES -> AppError.RealtimeDatabase.MaxRetries
                        DatabaseError.OVERRIDDEN_BY_SET -> AppError.RealtimeDatabase.OverriddenBySet
                        DatabaseError.UNAVAILABLE -> AppError.RealtimeDatabase.Unavailable
                        DatabaseError.WRITE_CANCELED -> AppError.RealtimeDatabase.WriteCanceled
                        DatabaseError.NETWORK_ERROR -> AppError.Generic.Network
                        DatabaseError.OPERATION_FAILED -> AppError.RealtimeDatabase.OperationFailed
                        else -> AppError.Generic.Unknown(error.toException())
                    }
                    trySend(DataResult.Failure(appError))
                    close()
                }
            }

            val entityLocationRef = locationsRef
                .child(entityType)
                .child(entityId)
            entityLocationRef.addValueEventListener(listener)

            awaitClose { entityLocationRef.removeEventListener(listener) }
        }
}
