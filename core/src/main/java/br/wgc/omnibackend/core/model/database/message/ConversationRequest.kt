package br.wgc.omnibackend.core.model.database.message

import androidx.annotation.Keep

/**
 * Representa o estado de uma conversa ou sala de chat em tempo real.
 *
 * @property conversationId Identificador exclusivo da sala de conversa.
 * @property type Modalidade da conversa ([ConversationType]).
 * @property participants Lista dos identificadores de usuários participantes.
 * @property lastMessage Última mensagem enviada na conversa, ou `null` caso recém-criada.
 * @property unreadCount Contagem de mensagens não lidas pelo usuário solicitante.
 */
@Keep
data class ConversationRequest(
    val conversationId: String = "",
    val type: ConversationType = ConversationType.ONE_TO_ONE,
    val participants: List<String> = emptyList(),
    val lastMessage: MessageRequest? = null,
    val unreadCount: Int = 0
)
