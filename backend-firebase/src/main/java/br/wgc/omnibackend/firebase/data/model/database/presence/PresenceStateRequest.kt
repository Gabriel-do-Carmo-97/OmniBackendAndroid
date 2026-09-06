package br.wgc.omnibackend.firebase.data.model.database.presence

import androidx.annotation.Keep

/**
 * Representa o estado de presença de uma entidade (online/offline).
 */
@Keep
data class PresenceStateRequest(
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis() // Timestamp da última mudança
)

