package br.wgc.omnibackend.core.model.database.message

import androidx.annotation.Keep

/**
 * Representa uma mensagem trafegada em uma conversa em tempo real.
 *
 * @property messageId Identificador único universal da mensagem.
 * @property senderId Identificador do usuário remetente.
 * @property content Conteúdo textual da mensagem.
 * @property timestamp Carimbo de data/hora em milissegundos UTC do envio.
 * @property status Estado de entrega atual da mensagem ([MessageStatus]).
 */
@Keep
data class MessageRequest(
    val messageId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENDING,
)
