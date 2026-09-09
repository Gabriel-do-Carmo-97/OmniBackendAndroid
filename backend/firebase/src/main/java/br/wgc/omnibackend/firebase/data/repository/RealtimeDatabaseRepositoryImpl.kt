package br.wgc.omnibackend.firebase.data.repository

import br.wgc.omnibackend.core.repository.RealtimeDatabaseRepository
import br.wgc.omnibackend.core.repository.realtime.GeolocationRepository
import br.wgc.omnibackend.core.repository.realtime.MessageRepository
import br.wgc.omnibackend.core.repository.realtime.PresenceRepository
import javax.inject.Inject

/**
 * Ponto de acesso aos sub-repositórios de mensageria, geolocalização e presença em tempo real.
 */
class RealtimeDatabaseRepositoryImpl @Inject constructor(
    private val messageRepository: MessageRepository,
    private val geolocationRepository: GeolocationRepository,
    private val presenceRepository: PresenceRepository,
) : RealtimeDatabaseRepository {

    /** Retorna o repositório de mensagens em tempo real. */
    override fun messages(): MessageRepository = messageRepository

    /** Retorna o repositório de geolocalização. */
    override fun geo(): GeolocationRepository = geolocationRepository

    /** Retorna o repositório de presença online/offline. */
    override fun presence(): PresenceRepository = presenceRepository
}
