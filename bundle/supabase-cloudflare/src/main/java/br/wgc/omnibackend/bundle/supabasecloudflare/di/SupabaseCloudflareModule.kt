package br.wgc.omnibackend.bundle.supabasecloudflare.di

import br.wgc.omnibackend.bundle.supabasecloudflare.OmniSupabaseCloudflare
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
 * Módulo Hilt para injeção direta do par Supabase + Cloudflare com failover.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseCloudflareModule {

    /** Qualificador por nome para repositórios híbridos Supabase + Cloudflare. */
    const val QUALIFIER_NAME = "supabase_cloudflare"

    /** Provê o [AuthRepository] híbrido Supabase + Cloudflare. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideAuthRepository(@SupabaseBackend primary: AuthRepository, @CloudflareBackend secondary: AuthRepository): AuthRepository =
        OmniSupabaseCloudflare.createAuth(primary, secondary)

    /** Provê o [FirestoreRepository] híbrido Supabase + Cloudflare. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideFirestoreRepository(
        @SupabaseBackend primary: FirestoreRepository,
        @CloudflareBackend secondary: FirestoreRepository,
    ): FirestoreRepository = OmniSupabaseCloudflare.createDatabase(primary, secondary)

    /** Provê o [StorageRepository] híbrido Supabase Storage + Cloudflare R2. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideStorageRepository(
        @SupabaseBackend primary: StorageRepository,
        @CloudflareBackend secondary: StorageRepository,
    ): StorageRepository = OmniSupabaseCloudflare.createStorage(primary, secondary)
}
