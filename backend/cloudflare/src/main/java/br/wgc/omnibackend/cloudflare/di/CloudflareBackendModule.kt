package br.wgc.omnibackend.cloudflare.di

import br.wgc.omnibackend.cloudflare.OmniCloudflare
import br.wgc.omnibackend.core.di.CloudflareBackend
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo Hilt corporativo para provimento de instâncias singleton do driver Cloudflare (Access, D1/KV, R2).
 *
 * Expõe as implementações concretas de [AuthRepository], [FirestoreRepository] e
 * [StorageRepository] qualificadas com [@CloudflareBackend] e [@Named("cloudflare")].
 */
@Module
@InstallIn(SingletonComponent::class)
object CloudflareBackendModule {

    /** Constante de qualificação por nome para o backend Cloudflare. */
    const val QUALIFIER_NAME = "cloudflare"

    /** Provê a instância de [AuthRepository] do Cloudflare Access/Workers Auth. */
    @Provides
    @Singleton
    @CloudflareBackend
    @Named(QUALIFIER_NAME)
    fun provideCloudflareAuthRepository(): AuthRepository = OmniCloudflare.auth

    /** Provê a instância de [FirestoreRepository] do Cloudflare D1/KV via REST. */
    @Provides
    @Singleton
    @CloudflareBackend
    @Named(QUALIFIER_NAME)
    fun provideCloudflareFirestoreRepository(): FirestoreRepository = OmniCloudflare.database

    /** Provê a instância de [StorageRepository] do Cloudflare R2 via S3 API. */
    @Provides
    @Singleton
    @CloudflareBackend
    @Named(QUALIFIER_NAME)
    fun provideCloudflareStorageRepository(): StorageRepository = OmniCloudflare.storage
}
