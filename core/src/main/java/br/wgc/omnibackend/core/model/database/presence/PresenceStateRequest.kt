package br.wgc.omnibackend.core.model.database.presence

import androidx.annotation.Keep

/**
 * Representa o estado de presença em tempo real de uma entidade (usuário ou dispositivo).
 *
 * @property isOnline Indica se a entidade está com conexão ativa no momento.
 * @property lastSeen Carimbo de data/hora em milissegundos UTC da última atividade ou desconexão registrada.
 */
@Keep
data class PresenceStateRequest(val isOnline: Boolean = false, val lastSeen: Long = System.currentTimeMillis())
