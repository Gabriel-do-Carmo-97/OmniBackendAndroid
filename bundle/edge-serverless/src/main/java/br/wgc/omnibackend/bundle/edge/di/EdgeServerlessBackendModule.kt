package br.wgc.omnibackend.bundle.edge.di

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.core.di.CloudflareBackend
import br.wgc.omnibackend.core.di.SupabaseBackend
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
 * Módulo Hilt corporativo para o bundle Edge-Serverless (:bundle:edge-serverless).
 *
 * Provê instâncias integrando Cloudflare Edge Workers / D1 / R2 com Supabase
 * para processamento descentralizado de ultra-baixa latência e persistência relacional.
 */
@Module
@InstallIn(SingletonComponent::class)
object EdgeServerlessBackendModule {

    /** Constante de qualificação por nome para failover Edge (Cloudflare + Supabase). */
    const val QUALIFIER_FAILOVER = "edge_failover"

    /**
     * Provê autenticação com failover entre Cloudflare Access/Workers e Supabase GoTrue.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideEdgeFailoverAuth(@CloudflareBackend primary: AuthRepository, @SupabaseBackend secondary: AuthRepository): AuthRepository =
        OmniHybrid.createAuth(primary, secondary)

    /**
     * Provê banco de dados com failover entre Cloudflare D1/KV e Supabase PostgREST.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideEdgeFailoverDatabase(
        @CloudflareBackend primary: FirestoreRepository,
        @SupabaseBackend secondary: FirestoreRepository,
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Provê armazenamento com failover entre Cloudflare R2 e Supabase Storage.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideEdgeFailoverStorage(
        @CloudflareBackend primary: StorageRepository,
        @SupabaseBackend secondary: StorageRepository,
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)
}
