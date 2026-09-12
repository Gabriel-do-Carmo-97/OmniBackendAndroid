package br.wgc.omnibackend.bundle.selfhosted.di

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.core.di.AppwriteBackend
import br.wgc.omnibackend.core.di.PocketBaseBackend
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
 * Módulo Hilt corporativo para o bundle Self-Hosted (:bundle:self-hosted).
 *
 * Provê instâncias integradas dos servidores independentes (PocketBase e Appwrite)
 * com suporte a failover e redundância para infraestruturas privadas e on-premises.
 */
@Module
@InstallIn(SingletonComponent::class)
object SelfHostedBackendModule {

    /** Constante de qualificação por nome para failover Self-Hosted (PocketBase + Appwrite). */
    const val QUALIFIER_FAILOVER = "self_hosted_failover"

    /**
     * Provê autenticação com failover entre PocketBase (primário) e Appwrite (secundário).
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideSelfHostedFailoverAuth(
        @PocketBaseBackend primary: AuthRepository,
        @AppwriteBackend secondary: AuthRepository
    ): AuthRepository = OmniHybrid.createAuth(primary, secondary)

    /**
     * Provê banco de dados com failover entre PocketBase (primário) e Appwrite (secundário).
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideSelfHostedFailoverDatabase(
        @PocketBaseBackend primary: FirestoreRepository,
        @AppwriteBackend secondary: FirestoreRepository
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Provê armazenamento com failover entre PocketBase (primário) e Appwrite (secundário).
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideSelfHostedFailoverStorage(
        @PocketBaseBackend primary: StorageRepository,
        @AppwriteBackend secondary: StorageRepository
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)
}
