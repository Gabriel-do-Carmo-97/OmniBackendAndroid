package br.wgc.omnibackend.rest.di

import br.wgc.omnibackend.core.di.RestBackend
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.rest.OmniRestBackend
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo Hilt corporativo para provimento de instâncias singleton do driver Custom REST.
 *
 * Expõe as implementações concretas de [AuthRepository], [FirestoreRepository] e
 * [StorageRepository] qualificadas com [@RestBackend] e [@Named("rest")].
 */
@Module
@InstallIn(SingletonComponent::class)
object RestBackendModule {

    /** Constante de qualificação por nome para o backend Custom REST. */
    const val QUALIFIER_NAME = "rest"

    /** Provê a instância de [AuthRepository] do Custom REST. */
    @Provides
    @Singleton
    @RestBackend
    @Named(QUALIFIER_NAME)
    fun provideRestAuthRepository(): AuthRepository = OmniRestBackend.auth

    /** Provê a instância de [FirestoreRepository] do Custom REST. */
    @Provides
    @Singleton
    @RestBackend
    @Named(QUALIFIER_NAME)
    fun provideRestFirestoreRepository(): FirestoreRepository = OmniRestBackend.database

    /** Provê a instância de [StorageRepository] do Custom REST. */
    @Provides
    @Singleton
    @RestBackend
    @Named(QUALIFIER_NAME)
    fun provideRestStorageRepository(): StorageRepository = OmniRestBackend.storage
}
