package br.wgc.omnibackend.supabase.di

import br.wgc.omnibackend.core.di.SupabaseBackend
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.supabase.OmniSupabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo Hilt corporativo para provimento de instâncias singleton do driver Supabase.
 *
 * Expõe as implementações concretas de [AuthRepository], [FirestoreRepository] e
 * [StorageRepository] qualificadas com [@SupabaseBackend] e [@Named("supabase")].
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseBackendModule {

    /** Constante de qualificação por nome para o backend Supabase. */
    const val QUALIFIER_NAME = "supabase"

    /** Provê a instância de [AuthRepository] do Supabase GoTrue. */
    @Provides
    @Singleton
    @SupabaseBackend
    @Named(QUALIFIER_NAME)
    fun provideSupabaseAuthRepository(): AuthRepository = OmniSupabase.auth

    /** Provê a instância de [FirestoreRepository] do Supabase PostgREST. */
    @Provides
    @Singleton
    @SupabaseBackend
    @Named(QUALIFIER_NAME)
    fun provideSupabaseFirestoreRepository(): FirestoreRepository = OmniSupabase.database

    /** Provê a instância de [StorageRepository] do Supabase Storage. */
    @Provides
    @Singleton
    @SupabaseBackend
    @Named(QUALIFIER_NAME)
    fun provideSupabaseStorageRepository(): StorageRepository = OmniSupabase.storage
}
