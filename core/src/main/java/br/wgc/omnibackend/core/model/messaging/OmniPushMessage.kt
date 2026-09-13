package br.wgc.omnibackend.core.model.messaging

/**
 * Modelo agnóstico de mensagem de notificação push.
 */
data class OmniPushMessage(
    val id: String,
    val title: String?,
    val body: String?,
    val imageUrl: String? = null,
    val dataPayload: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
)
