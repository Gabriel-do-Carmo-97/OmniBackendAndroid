package br.wgc.omnibackend.core.model.database.message

import androidx.annotation.Keep

/**
 * Representa os estágios de ciclo de vida e entrega de uma mensagem no chat.
 */
@Keep
enum class MessageStatus {
    /** Mensagem sendo processada ou enviada localmente pelo cliente. */
    SENDING,

    /** Mensagem recebida com sucesso pelo servidor de banco de dados. */
    SENT,

    /** Mensagem entregue no dispositivo do destinatário. */
    DELIVERED,

    /** Mensagem visualizada / lida pelo destinatário. */
    READ,

    /** Falha no envio ou processamento da mensagem. */
    FAILED,
}
