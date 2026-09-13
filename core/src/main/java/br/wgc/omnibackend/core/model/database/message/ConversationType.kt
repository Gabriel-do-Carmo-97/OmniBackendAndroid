package br.wgc.omnibackend.core.model.database.message

import androidx.annotation.Keep

/**
 * Define a tipologia e a modalidade de uma conversa de mensageria em tempo real.
 */
@Keep
enum class ConversationType {
    /** Conversa direta entre dois participantes (chat 1-a-1). */
    ONE_TO_ONE,

    /** Conversa em grupo envolvendo múltiplos participantes. */
    GROUP,

    /** Atendimento ou suporte direto entre cliente e equipe de assistência. */
    SUPPORT,
}
