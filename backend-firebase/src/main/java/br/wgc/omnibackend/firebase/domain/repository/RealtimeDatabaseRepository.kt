package br.wgc.omnibackend.firebase.domain.repository

import br.wgc.omnibackend.firebase.domain.repository.realtime.GeolocationRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.MessageRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.PresenceRepository


interface RealtimeDatabaseRepository {
    fun messages(): MessageRepository
    fun geo(): GeolocationRepository
    fun presence(): PresenceRepository
}

