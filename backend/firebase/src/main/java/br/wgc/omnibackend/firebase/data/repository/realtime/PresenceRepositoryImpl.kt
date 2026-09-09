package br.wgc.omnibackend.firebase.data.repository.realtime

import br.wgc.omnibackend.core.model.database.presence.PresenceStateRequest
import br.wgc.omnibackend.core.repository.realtime.PresenceRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Implementação do contrato [PresenceRepository] para controle de presença online/offline.
 *
 * Utiliza nós especiais `.info/connected` e hooks de `onDisconnect` do Firebase Realtime Database.
 *
 * @property database Instância do [FirebaseDatabase] injetada.
 */
class PresenceRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
) : PresenceRepository {
    private val presenceRef = database.getReference("presence")
    private val connectedRef = database.getReference(".info/connected")
    private val activeConnectedListeners = ConcurrentHashMap<String, ValueEventListener>()

    /**
     * Marca a entidade como online e registra gancho para transição automática para offline ao desconectar.
     */
    override suspend fun goOnline(entityType: String, entityId: String): DataResult<Unit> = runCatching {
        val key = "$entityType/$entityId"
        val entityPresenceRef = presenceRef.child(entityType).child(entityId)

        activeConnectedListeners.remove(key)?.let { oldListener ->
            connectedRef.removeEventListener(oldListener)
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue(Boolean::class.java) == true) {
                    entityPresenceRef.setValue(PresenceStateRequest(isOnline = true))
                    entityPresenceRef.onDisconnect().setValue(PresenceStateRequest(isOnline = false))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Conexão cancelada
            }
        }

        activeConnectedListeners[key] = listener
        connectedRef.addValueEventListener(listener)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    /**
     * Marca a entidade explicitamente como offline e remove os ganchos pendentes.
     */
    override suspend fun goOffline(entityType: String, entityId: String): DataResult<Unit> = runCatching {
        val key = "$entityType/$entityId"
        activeConnectedListeners.remove(key)?.let { listener ->
            connectedRef.removeEventListener(listener)
        }

        val entityPresenceRef = presenceRef.child(entityType).child(entityId)
        entityPresenceRef.onDisconnect().cancel()
        entityPresenceRef.setValue(PresenceStateRequest(isOnline = false)).await()
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(mapExceptionToAppError(it))
    }

    /**
     * Monitora o estado de presença em tempo real.
     */
    override fun trackPresence(entityType: String, entityId: String): Flow<DataResult<PresenceStateRequest>> = callbackFlow {
        val entityPresenceRef = presenceRef.child(entityType).child(entityId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(PresenceStateRequest::class.java)?.let {
                    trySend(DataResult.Success(it))
                } ?: trySend(DataResult.Success(PresenceStateRequest(isOnline = false)))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(DataResult.Failure(mapDatabaseErrorToAppError(error)))
                close()
            }
        }
        entityPresenceRef.addValueEventListener(listener)
        awaitClose { entityPresenceRef.removeEventListener(listener) }
    }

    private fun mapExceptionToAppError(exception: Throwable): AppError {
        return when (exception) {
            is DatabaseException -> {
                when {
                    exception.message?.contains("permission_denied", ignoreCase = true) == true ->
                        AppError.RealtimeDatabase.PermissionDenied
                    else -> AppError.RealtimeDatabase.OperationFailed
                }
            }
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
    }

    private fun mapDatabaseErrorToAppError(error: DatabaseError): AppError {
        return when (error.code) {
            DatabaseError.PERMISSION_DENIED -> AppError.RealtimeDatabase.PermissionDenied
            else -> AppError.Generic.Unknown(error.toException())
        }
    }
}
