package br.wgc.omnibackend.bundle.hybrid.di

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.core.di.ActiveBackend
import br.wgc.omnibackend.core.di.FirebaseBackend
import br.wgc.omnibackend.core.di.OmniBackendSelector
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
 * Módulo Hilt corporativo para o bundle híbrido (:bundle:hybrid).
 *
 * Provê instâncias ativas e combinadas de [AuthRepository], [FirestoreRepository] e [StorageRepository]
 * com suporte a failover automático e orquestração dinâmica via [OmniBackendSelector].
 */
@Module
@InstallIn(SingletonComponent::class)
object HybridBackendModule {

    /** Constante de qualificação por nome para repositórios híbridos com failover. */
    const val QUALIFIER_FAILOVER = "hybrid_failover"

    /** Constante de qualificação por nome para o backend ativo. */
    const val QUALIFIER_ACTIVE = "active"

    /**
     * Provê o [AuthRepository] híbrido com failover automático do Firebase (primário) para o Supabase (secundário).
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideHybridFailoverAuthRepository(
        @FirebaseBackend primary: AuthRepository,
        @SupabaseBackend secondary: AuthRepository,
    ): AuthRepository = OmniHybrid.createAuth(primary, secondary)

    /**
     * Provê o [FirestoreRepository] híbrido com failover do Firestore para o PostgREST.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideHybridFailoverFirestoreRepository(
        @FirebaseBackend primary: FirestoreRepository,
        @SupabaseBackend secondary: FirestoreRepository,
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Provê o [StorageRepository] híbrido com failover do Firebase Storage para o Supabase Storage.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideHybridFailoverStorageRepository(
        @FirebaseBackend primary: StorageRepository,
        @SupabaseBackend secondary: StorageRepository,
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)

    /**
     * Provê o [AuthRepository] ativo da aplicação, resolvido dinamicamente pelo [OmniBackendSelector]
     * ou caindo para o provedor primário Firebase.
     */
    @Provides
    @Singleton
    @ActiveBackend
    @Named(QUALIFIER_ACTIVE)
    fun provideActiveAuthRepository(@FirebaseBackend defaultPrimary: AuthRepository): AuthRepository =
        OmniBackendSelector.getActiveAuthRepository() ?: defaultPrimary

    /**
     * Provê o [FirestoreRepository] ativo da aplicação, resolvido dinamicamente pelo [OmniBackendSelector]
     * ou caindo para o provedor primário Firebase.
     */
    @Provides
    @Singleton
    @ActiveBackend
    @Named(QUALIFIER_ACTIVE)
    fun provideActiveFirestoreRepository(@FirebaseBackend defaultPrimary: FirestoreRepository): FirestoreRepository =
        OmniBackendSelector.getActiveFirestoreRepository() ?: defaultPrimary

    /**
     * Provê o [StorageRepository] ativo da aplicação, resolvido dinamicamente pelo [OmniBackendSelector]
     * ou caindo para o provedor primário Firebase.
     */
    @Provides
    @Singleton
    @ActiveBackend
    @Named(QUALIFIER_ACTIVE)
    fun provideActiveStorageRepository(@FirebaseBackend defaultPrimary: StorageRepository): StorageRepository =
        OmniBackendSelector.getActiveStorageRepository() ?: defaultPrimary
}
