package br.wgc.omnibackend.firebase.data.repository.realtime

import br.wgc.omnibackend.firebase.domain.repository.realtime.MessageRepository
import br.wgc.omnibackend.firebase.utils.AppError
import br.wgc.omnibackend.firebase.utils.DataResult
import br.wgc.omnibackend.firebase.data.model.database.message.ConversationRequest
import br.wgc.omnibackend.firebase.data.model.database.message.MessageRequest
import br.wgc.omnibackend.firebase.data.model.database.message.MessageStatus
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

class MessageRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
) : MessageRepository {
    private val messagesRef = database.getReference("messages")
    private val conversationsRef = database.getReference("conversations")
    private val usersRef = database.getReference("users") // Assumindo um nó 'users' para busca

    override suspend fun sendMessage(
        conversationId: String,
        message: MessageRequest
    ): DataResult<Unit> = runCatching {
        val messageId = messagesRef.child(conversationId).push().key
            ?: throw IllegalStateException("Não foi possível gerar a chave da mensagem.")

        val messageToSend = message.copy(messageId = messageId, status = MessageStatus.SENT)

        val updates = mapOf(
            "/messages/$conversationId/$messageId" to messageToSend,
            "/conversations/$conversationId/lastMessage" to messageToSend
        )

        database.reference.updateChildren(updates).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        val appError = mapExceptionToAppError(exception)
        DataResult.Failure(appError)
    }

    override fun getMessages(conversationId: String): Flow<DataResult<List<MessageRequest>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue<MessageRequest>() }
                trySend(DataResult.Success(messages))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(DataResult.Failure(mapDatabaseErrorToAppError(error)))
                close()
            }
        }
        messagesRef.child(conversationId).addValueEventListener(listener)
        awaitClose { messagesRef.child(conversationId).removeEventListener(listener) }
    }

    override suspend fun getMessageHistory(
        conversationId: String,
        lastMessageId: String?,
        limit: Int
    ): DataResult<List<MessageRequest>> = runCatching {
        var query = messagesRef.child(conversationId).orderByKey().limitToLast(limit)
        if (lastMessageId != null) {
            query = query.endBefore(lastMessageId)
        }
        val snapshot = query.get().await()
        val messages = snapshot.children.mapNotNull { it.getValue<MessageRequest>() }
        DataResult.Success(messages)
    }.getOrElse { exception ->
        DataResult.Failure(mapExceptionToAppError(exception))
    }

    override fun getConversations(userId: String): Flow<DataResult<List<ConversationRequest>>> = callbackFlow {
        val userConversationsRef = database.getReference("user-conversations").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = snapshot.children.mapNotNull { it.getValue<ConversationRequest>() }
                trySend(DataResult.Success(conversations))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(DataResult.Failure(mapDatabaseErrorToAppError(error)))
                close()
            }
        }
        userConversationsRef.addValueEventListener(listener)
        awaitClose { userConversationsRef.removeEventListener(listener) }
    }

    override suspend fun createConversation(conversation: ConversationRequest): DataResult<String> = runCatching {
        val conversationId = conversationsRef.push().key
            ?: throw IllegalStateException("Não foi possível gerar a chave da conversa.")
        
        val conversationToCreate = conversation.copy(conversationId = conversationId)
        
        val updates = mutableMapOf<String, Any?>()
        updates["/conversations/$conversationId"] = conversationToCreate
        conversation.participants.forEach { userId ->
            updates["/user-conversations/$userId/$conversationId"] = conversationToCreate
        }
        
        database.reference.updateChildren(updates).await()
        DataResult.Success(conversationId)
    }.getOrElse { exception ->
         DataResult.Failure(mapExceptionToAppError(exception))
    }

    override suspend fun searchUsers(query: String): DataResult<List<Any>> = runCatching {
        val snapshot = usersRef.orderByChild("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limitToFirst(20)
            .get().await()
        
        val users = snapshot.children.mapNotNull { it.getValue<Any>() }
        DataResult.Success(users)
    }.getOrElse { exception ->
        DataResult.Failure(mapExceptionToAppError(exception))
    }

    override suspend fun updateMessageStatus(
        conversationId: String,
        messageId: String,
        status: MessageStatus
    ): DataResult<Unit> = runCatching {
        val updates = mapOf(
            "/messages/$conversationId/$messageId/status" to status,
        )
        database.reference.updateChildren(updates).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        DataResult.Failure(mapExceptionToAppError(exception))
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
    }
}

