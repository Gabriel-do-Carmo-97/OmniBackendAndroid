package br.wgc.omnibackend.pocketbase.di

import br.wgc.omnibackend.core.di.PocketBaseBackend
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.pocketbase.OmniPocketBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo Hilt corporativo para provimento de instâncias singleton do driver PocketBase.
 *
 * Expõe as implementações concretas de [AuthRepository], [FirestoreRepository] e
 * [StorageRepository] qualificadas com [@PocketBaseBackend] e [@Named("pocketbase")].
 */
@Module
@InstallIn(SingletonComponent::class)
object PocketbaseBackendModule {

    /** Constante de qualificação por nome para o backend PocketBase. */
    const val QUALIFIER_NAME = "pocketbase"

    /** Provê a instância de [AuthRepository] do PocketBase Users. */
    @Provides
    @Singleton
    @PocketBaseBackend
    @Named(QUALIFIER_NAME)
    fun providePocketBaseAuthRepository(): AuthRepository = OmniPocketBase.auth

    /** Provê a instância de [FirestoreRepository] do PocketBase Collections. */
    @Provides
    @Singleton
    @PocketBaseBackend
    @Named(QUALIFIER_NAME)
    fun providePocketBaseFirestoreRepository(): FirestoreRepository = OmniPocketBase.database

    /** Provê a instância de [StorageRepository] do PocketBase Files. */
    @Provides
    @Singleton
    @PocketBaseBackend
    @Named(QUALIFIER_NAME)
    fun providePocketBaseStorageRepository(): StorageRepository = OmniPocketBase.storage
}
