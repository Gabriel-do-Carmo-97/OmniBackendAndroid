package br.wgc.omnibackend.firebase.di

import br.wgc.omnibackend.core.di.FirebaseBackend
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.firebase.OmniFirebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo Hilt corporativo para provimento de instâncias singleton do driver Firebase.
 *
 * Expõe as implementações concretas de [AuthRepository], [FirestoreRepository] e
 * [StorageRepository] qualificadas com [@FirebaseBackend] e [@Named("firebase")].
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseBackendModule {

    /** Constante de qualificação por nome para o backend Firebase. */
    const val QUALIFIER_NAME = "firebase"

    /** Provê a instância de [AuthRepository] do Firebase. */
    @Provides
    @Singleton
    @FirebaseBackend
    @Named(QUALIFIER_NAME)
    fun provideFirebaseAuthRepository(): AuthRepository = OmniFirebase.auth

    /** Provê a instância de [FirestoreRepository] do Firebase. */
    @Provides
    @Singleton
    @FirebaseBackend
    @Named(QUALIFIER_NAME)
    fun provideFirebaseFirestoreRepository(): FirestoreRepository = OmniFirebase.firestore

    /** Provê a instância de [StorageRepository] do Firebase. */
    @Provides
    @Singleton
    @FirebaseBackend
    @Named(QUALIFIER_NAME)
    fun provideFirebaseStorageRepository(): StorageRepository = OmniFirebase.storage
}
