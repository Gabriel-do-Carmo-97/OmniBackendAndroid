package br.wgc.omnibackend.firebase.data.repository.realtime

import br.wgc.omnibackend.core.model.database.message.ConversationRequest
import br.wgc.omnibackend.core.model.database.message.MessageRequest
import br.wgc.omnibackend.core.model.database.message.MessageStatus
import br.wgc.omnibackend.core.repository.realtime.MessageRepository
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
 * Implementação do contrato [MessageRepository] sobre o Firebase Realtime Database.
 *
 * @property database Instância do [FirebaseDatabase] injetada.
 */
class MessageRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
) : MessageRepository {
    private val messagesRef = database.getReference("messages")
    private val conversationsRef = database.getReference("conversations")
    private val usersRef = database.getReference("users")

    /**
     * Envia uma mensagem e atualiza atomicamente a última mensagem da conversa.
     *
     * @param conversationId Identificador da conversa.
     * @param message Dados da mensagem.
     */
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

    /**
     * Escuta mensagens de uma conversa em tempo real via snapshot listener.
     */
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
        val conversationMessagesRef = messagesRef.child(conversationId)
        conversationMessagesRef.addValueEventListener(listener)
        awaitClose { conversationMessagesRef.removeEventListener(listener) }
    }

    /**
     * Obtém o histórico paginado de mensagens.
     */
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

    /**
     * Ouve as conversas de um usuário em tempo real.
     */
    override fun getConversations(userId: String): Flow<DataResult<List<ConversationRequest>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = snapshot.children.mapNotNull { it.getValue<ConversationRequest>() }
                    .filter { it.participants.contains(userId) }
                trySend(DataResult.Success(conversations))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(DataResult.Failure(mapDatabaseErrorToAppError(error)))
                close()
            }
        }
        conversationsRef.addValueEventListener(listener)
        awaitClose { conversationsRef.removeEventListener(listener) }
    }

    /**
     * Cria uma nova conversa no Realtime Database.
     */
    override suspend fun createConversation(conversation: ConversationRequest): DataResult<String> = runCatching {
        val conversationId = conversationsRef.push().key
            ?: throw IllegalStateException("Não foi possível gerar a chave da conversa.")
        val newConversation = conversation.copy(conversationId = conversationId)
        conversationsRef.child(conversationId).setValue(newConversation).await()
        DataResult.Success(conversationId)
    }.getOrElse { exception ->
        DataResult.Failure(mapExceptionToAppError(exception))
    }

    /**
     * Busca participantes por nome ou e-mail.
     */
    override suspend fun searchUsers(query: String): DataResult<List<Any>> = runCatching {
        val snapshot = usersRef.orderByChild("name").startAt(query).endAt(query + "\uf8ff").get().await()
        val users = snapshot.children.mapNotNull { it.value }
        DataResult.Success(users)
    }.getOrElse { exception ->
        DataResult.Failure(mapExceptionToAppError(exception))
    }

    /**
     * Atualiza o status de entrega de uma mensagem.
     */
    override suspend fun updateMessageStatus(
        conversationId: String,
        messageId: String,
        status: MessageStatus
    ): DataResult<Unit> = runCatching {
        messagesRef.child(conversationId).child(messageId).child("status").setValue(status).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        DataResult.Failure(mapExceptionToAppError(exception))
    }

    private fun mapExceptionToAppError(exception: Throwable): AppError {
        return when (exception) {
            is DatabaseException -> AppError.RealtimeDatabase.OperationFailed
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
