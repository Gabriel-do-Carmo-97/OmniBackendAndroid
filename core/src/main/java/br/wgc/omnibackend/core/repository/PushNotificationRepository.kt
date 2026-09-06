package br.wgc.omnibackend.core.repository

import br.wgc.omnibackend.core.model.messaging.OmniPushMessage
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato agnóstico para gerenciamento de notificações push (FCM, UnifiedPush, PushKit).
 */
interface PushNotificationRepository {

    /**
     * Fluxo reativo de mensagens push recebidas em tempo real.
     */
    val notificationEvents: Flow<OmniPushMessage>

    /**
     * Obtém o token de registro de push atual do dispositivo.
     */
    suspend fun getPushToken(): DataResult<String>

    /**
     * Inscreve o dispositivo em um tópico de notificação.
     */
    suspend fun subscribeToTopic(topic: String): DataResult<Unit>

    /**
     * Cancela a inscrição do dispositivo em um tópico.
     */
    suspend fun unsubscribeFromTopic(topic: String): DataResult<Unit>
}
