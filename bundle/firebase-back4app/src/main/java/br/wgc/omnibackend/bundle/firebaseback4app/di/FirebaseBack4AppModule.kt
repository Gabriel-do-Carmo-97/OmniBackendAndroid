package br.wgc.omnibackend.bundle.firebaseback4app.di

import br.wgc.omnibackend.bundle.firebaseback4app.OmniFirebaseBack4App
import br.wgc.omnibackend.core.di.Back4AppBackend
import br.wgc.omnibackend.core.di.FirebaseBackend
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
 * Módulo Hilt para injeção direta do par Firebase + Back4App com failover.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseBack4AppModule {

    /** Qualificador por nome para repositórios híbridos Firebase + Back4App. */
    const val QUALIFIER_NAME = "firebase_back4app"

    /** Provê o [AuthRepository] híbrido Firebase + Back4App. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideAuthRepository(
        @FirebaseBackend primary: AuthRepository,
        @Back4AppBackend secondary: AuthRepository
    ): AuthRepository = OmniFirebaseBack4App.createAuth(primary, secondary)

    /** Provê o [FirestoreRepository] híbrido Firestore + Back4App Parse. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideFirestoreRepository(
        @FirebaseBackend primary: FirestoreRepository,
        @Back4AppBackend secondary: FirestoreRepository
    ): FirestoreRepository = OmniFirebaseBack4App.createDatabase(primary, secondary)

    /** Provê o [StorageRepository] híbrido Firebase Storage + Back4App ParseFile. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideStorageRepository(
        @FirebaseBackend primary: StorageRepository,
        @Back4AppBackend secondary: StorageRepository
    ): StorageRepository = OmniFirebaseBack4App.createStorage(primary, secondary)
}
