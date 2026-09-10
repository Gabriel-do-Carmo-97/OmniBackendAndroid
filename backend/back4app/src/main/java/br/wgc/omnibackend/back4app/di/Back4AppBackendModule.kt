package br.wgc.omnibackend.back4app.di

import br.wgc.omnibackend.back4app.OmniBack4App
import br.wgc.omnibackend.core.di.Back4AppBackend
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
 * Módulo Hilt corporativo para provimento de instâncias singleton do driver Back4App (Parse Platform).
 *
 * Expõe as implementações concretas de [AuthRepository], [FirestoreRepository] e
 * [StorageRepository] qualificadas com [@Back4AppBackend] e [@Named("back4app")].
 */
@Module
@InstallIn(SingletonComponent::class)
object Back4AppBackendModule {

    /** Constante de qualificação por nome para o backend Back4App. */
    const val QUALIFIER_NAME = "back4app"

    /** Provê a instância de [AuthRepository] do Back4App ParseUser. */
    @Provides
    @Singleton
    @Back4AppBackend
    @Named(QUALIFIER_NAME)
    fun provideBack4AppAuthRepository(): AuthRepository = OmniBack4App.auth

    /** Provê a instância de [FirestoreRepository] do Back4App ParseObject. */
    @Provides
    @Singleton
    @Back4AppBackend
    @Named(QUALIFIER_NAME)
    fun provideBack4AppFirestoreRepository(): FirestoreRepository = OmniBack4App.database

    /** Provê a instância de [StorageRepository] do Back4App ParseFile. */
    @Provides
    @Singleton
    @Back4AppBackend
    @Named(QUALIFIER_NAME)
    fun provideBack4AppStorageRepository(): StorageRepository = OmniBack4App.storage
}
