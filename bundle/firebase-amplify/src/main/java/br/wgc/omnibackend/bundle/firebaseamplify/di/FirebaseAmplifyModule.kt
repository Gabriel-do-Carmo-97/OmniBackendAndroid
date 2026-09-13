package br.wgc.omnibackend.bundle.firebaseamplify.di

import br.wgc.omnibackend.bundle.firebaseamplify.OmniFirebaseAmplify
import br.wgc.omnibackend.core.di.AmplifyBackend
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
 * Módulo Hilt para injeção direta do par multi-cloud Firebase + AWS Amplify.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseAmplifyModule {

    /** Qualificador por nome para repositórios multi-cloud Firebase + Amplify. */
    const val QUALIFIER_NAME = "firebase_amplify"

    /** Provê o [AuthRepository] multi-cloud Firebase + AWS Cognito. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideAuthRepository(@FirebaseBackend primary: AuthRepository, @AmplifyBackend secondary: AuthRepository): AuthRepository =
        OmniFirebaseAmplify.createAuth(primary, secondary)

    /** Provê o [FirestoreRepository] multi-cloud Firestore + DynamoDB/AppSync. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideFirestoreRepository(
        @FirebaseBackend primary: FirestoreRepository,
        @AmplifyBackend secondary: FirestoreRepository,
    ): FirestoreRepository = OmniFirebaseAmplify.createDatabase(primary, secondary)

    /** Provê o [StorageRepository] multi-cloud Firebase Storage + Amazon S3. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideStorageRepository(
        @FirebaseBackend primary: StorageRepository,
        @AmplifyBackend secondary: StorageRepository,
    ): StorageRepository = OmniFirebaseAmplify.createStorage(primary, secondary)
}
