package br.wgc.omnibackend.core.repository.realtime

import br.wgc.omnibackend.core.model.database.message.ConversationRequest
import br.wgc.omnibackend.core.model.database.message.MessageRequest
import br.wgc.omnibackend.core.model.database.message.MessageStatus
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato agnóstico para chat, conversas e troca de mensagens em tempo real.
 */
interface MessageRepository {

    /**
     * Envia uma mensagem para a conversa especificada.
     *
     * @param conversationId Identificador da conversa de destino.
     * @param message Payload da mensagem a ser transmitida ([MessageRequest]).
     * @return [DataResult.Success] com [Unit] após persistência da mensagem.
     */
    suspend fun sendMessage(conversationId: String, message: MessageRequest): DataResult<Unit>

    /**
     * Ouve as mensagens de uma conversa em tempo real.
     *
     * @param conversationId Identificador da conversa.
     * @return [Flow] que emite a lista de mensagens sempre que novas mensagens chegam.
     */
    fun getMessages(conversationId: String): Flow<DataResult<List<MessageRequest>>>

    /**
     * Recupera o histórico de mensagens de forma paginada.
     *
     * @param conversationId Identificador da conversa.
     * @param lastMessageId ID da última mensagem previamente carregada (marcador de paginação), ou `null` para as mais recentes.
     * @param limit Quantidade máxima de registros a retornar (padrão 50).
     * @return [DataResult.Success] contendo o lote de mensagens.
     */
    suspend fun getMessageHistory(conversationId: String, lastMessageId: String?, limit: Int = 50): DataResult<List<MessageRequest>>

    /**
     * Observa a lista de conversas ativas de um usuário em tempo real.
     *
     * @param userId Identificador do usuário.
     * @return [Flow] emitindo a lista atualizada de conversas.
     */
    fun getConversations(userId: String): Flow<DataResult<List<ConversationRequest>>>

    /**
     * Cria uma nova sala ou conversa de chat.
     *
     * @param conversation Parâmetros da conversa ([ConversationRequest]).
     * @return [DataResult.Success] com o identificador exclusivo gerado para a conversa.
     */
    suspend fun createConversation(conversation: ConversationRequest): DataResult<String>

    /**
     * Realiza busca de participantes no ecossistema de mensageria.
     *
     * @param query Termo de busca (nome ou e-mail).
     * @return [DataResult.Success] contendo os participantes localizados.
     */
    suspend fun searchUsers(query: String): DataResult<List<Any>>

    /**
     * Atualiza o status de entrega/leitura de uma mensagem.
     *
     * @param conversationId Identificador da conversa.
     * @param messageId Identificador da mensagem.
     * @param status Novo estado da mensagem ([MessageStatus]).
     * @return [DataResult.Success] com [Unit] após a alteração.
     */
    suspend fun updateMessageStatus(conversationId: String, messageId: String, status: MessageStatus): DataResult<Unit>
}
