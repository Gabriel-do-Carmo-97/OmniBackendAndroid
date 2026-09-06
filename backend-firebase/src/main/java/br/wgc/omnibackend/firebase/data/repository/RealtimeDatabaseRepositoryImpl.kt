package br.wgc.omnibackend.firebase.data.repository

import br.wgc.omnibackend.firebase.domain.repository.RealtimeDatabaseRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.GeolocationRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.MessageRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.PresenceRepository
import javax.inject.Inject

internal class RealtimeDatabaseRepositoryImpl @Inject constructor(
    private val messageRepository: MessageRepository,
    private val geolocationRepository: GeolocationRepository,
    private val presenceRepository: PresenceRepository,
) : RealtimeDatabaseRepository {

    override fun messages(): MessageRepository = messageRepository
    override fun geo(): GeolocationRepository = geolocationRepository
    override fun presence(): PresenceRepository = presenceRepository

}
