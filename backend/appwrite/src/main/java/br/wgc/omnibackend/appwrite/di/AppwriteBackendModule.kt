package br.wgc.omnibackend.appwrite.di

import br.wgc.omnibackend.appwrite.OmniAppwrite
import br.wgc.omnibackend.core.di.AppwriteBackend
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
 * Módulo Hilt corporativo para provimento de instâncias singleton do driver Appwrite.
 *
 * Expõe as implementações concretas de [AuthRepository], [FirestoreRepository] e
 * [StorageRepository] qualificadas com [@AppwriteBackend] e [@Named("appwrite")].
 */
@Module
@InstallIn(SingletonComponent::class)
object AppwriteBackendModule {

    /** Constante de qualificação por nome para o backend Appwrite. */
    const val QUALIFIER_NAME = "appwrite"

    /** Provê a instância de [AuthRepository] do Appwrite Account. */
    @Provides
    @Singleton
    @AppwriteBackend
    @Named(QUALIFIER_NAME)
    fun provideAppwriteAuthRepository(): AuthRepository = OmniAppwrite.auth

    /** Provê a instância de [FirestoreRepository] do Appwrite Databases. */
    @Provides
    @Singleton
    @AppwriteBackend
    @Named(QUALIFIER_NAME)
    fun provideAppwriteFirestoreRepository(): FirestoreRepository = OmniAppwrite.database

    /** Provê a instância de [StorageRepository] do Appwrite Storage. */
    @Provides
    @Singleton
    @AppwriteBackend
    @Named(QUALIFIER_NAME)
    fun provideAppwriteStorageRepository(): StorageRepository = OmniAppwrite.storage
}
