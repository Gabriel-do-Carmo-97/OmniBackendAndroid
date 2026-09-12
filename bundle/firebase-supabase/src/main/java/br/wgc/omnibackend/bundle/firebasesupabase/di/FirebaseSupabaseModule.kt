package br.wgc.omnibackend.bundle.firebasesupabase.di

import br.wgc.omnibackend.bundle.firebasesupabase.OmniFirebaseSupabase
import br.wgc.omnibackend.core.di.FirebaseBackend
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
 * Módulo Hilt para injeção direta do par Firebase + Supabase com failover.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseSupabaseModule {

    /** Qualificador por nome para repositórios híbridos Firebase + Supabase. */
    const val QUALIFIER_NAME = "firebase_supabase"

    /** Provê o [AuthRepository] híbrido Firebase + Supabase. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideAuthRepository(
        @FirebaseBackend primary: AuthRepository,
        @SupabaseBackend secondary: AuthRepository
    ): AuthRepository = OmniFirebaseSupabase.createAuth(primary, secondary)

    /** Provê o [FirestoreRepository] híbrido Firestore + PostgREST. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideFirestoreRepository(
        @FirebaseBackend primary: FirestoreRepository,
        @SupabaseBackend secondary: FirestoreRepository
    ): FirestoreRepository = OmniFirebaseSupabase.createDatabase(primary, secondary)

    /** Provê o [StorageRepository] híbrido Firebase Storage + Supabase Storage. */
    @Provides
    @Singleton
    @Named(QUALIFIER_NAME)
    fun provideStorageRepository(
        @FirebaseBackend primary: StorageRepository,
        @SupabaseBackend secondary: StorageRepository
    ): StorageRepository = OmniFirebaseSupabase.createStorage(primary, secondary)
}
