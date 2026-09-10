package br.wgc.omnibackend.amplify.di

import br.wgc.omnibackend.amplify.OmniAmplify
import br.wgc.omnibackend.core.di.AmplifyBackend
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
 * Módulo Hilt corporativo para provimento de instâncias singleton do driver AWS Amplify.
 *
 * Expõe as implementações concretas de [AuthRepository], [FirestoreRepository] e
 * [StorageRepository] qualificadas com [@AmplifyBackend] e [@Named("amplify")].
 */
@Module
@InstallIn(SingletonComponent::class)
object AmplifyBackendModule {

    /** Constante de qualificação por nome para o backend AWS Amplify. */
    const val QUALIFIER_NAME = "amplify"

    /** Provê a instância de [AuthRepository] do AWS Cognito via Amplify. */
    @Provides
    @Singleton
    @AmplifyBackend
    @Named(QUALIFIER_NAME)
    fun provideAmplifyAuthRepository(): AuthRepository = OmniAmplify.auth

    /** Provê a instância de [FirestoreRepository] do AWS AppSync/DynamoDB via Amplify. */
    @Provides
    @Singleton
    @AmplifyBackend
    @Named(QUALIFIER_NAME)
    fun provideAmplifyFirestoreRepository(): FirestoreRepository = OmniAmplify.database

    /** Provê a instância de [StorageRepository] do AWS S3 via Amplify. */
    @Provides
    @Singleton
    @AmplifyBackend
    @Named(QUALIFIER_NAME)
    fun provideAmplifyStorageRepository(): StorageRepository = OmniAmplify.storage
}
