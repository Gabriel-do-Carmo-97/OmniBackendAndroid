package br.wgc.omnibackend.core.repository

import br.wgc.omnibackend.core.repository.realtime.GeolocationRepository
import br.wgc.omnibackend.core.repository.realtime.MessageRepository
import br.wgc.omnibackend.core.repository.realtime.PresenceRepository

/**
 * Ponto de acesso unificado aos serviços e sub-repositórios de banco de dados em tempo real.
 */
interface RealtimeDatabaseRepository {

    /** Retorna o sub-repositório para envio e escuta de mensagens em tempo real. */
    fun messages(): MessageRepository

    /** Retorna o sub-repositório para rastreamento de coordenadas geográficas. */
    fun geo(): GeolocationRepository

    /** Retorna o sub-repositório para monitoramento de presença online/offline. */
    fun presence(): PresenceRepository
}
