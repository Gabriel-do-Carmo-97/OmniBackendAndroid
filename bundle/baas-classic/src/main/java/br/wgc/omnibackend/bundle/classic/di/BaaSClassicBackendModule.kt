package br.wgc.omnibackend.bundle.classic.di

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
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
 * Módulo Hilt corporativo para o bundle BaaS-Classic (:bundle:baas-classic).
 *
 * Provê instâncias integrando Back4App (Parse Platform) com Firebase
 * para aplicações que utilizam a arquitetura tradicional de BaaS NoSQL/Parse com redundância.
 */
@Module
@InstallIn(SingletonComponent::class)
object BaaSClassicBackendModule {

    /** Constante de qualificação por nome para failover Clássico (Back4App + Firebase). */
    const val QUALIFIER_FAILOVER = "classic_failover"

    /**
     * Provê autenticação com failover entre Back4App (primário) e Firebase (secundário).
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideClassicFailoverAuth(
        @Back4AppBackend primary: AuthRepository,
        @FirebaseBackend secondary: AuthRepository
    ): AuthRepository = OmniHybrid.createAuth(primary, secondary)

    /**
     * Provê banco de dados com failover entre Back4App e Firebase Firestore.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideClassicFailoverDatabase(
        @Back4AppBackend primary: FirestoreRepository,
        @FirebaseBackend secondary: FirestoreRepository
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Provê armazenamento com failover entre Back4App e Firebase Storage.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideClassicFailoverStorage(
        @Back4AppBackend primary: StorageRepository,
        @FirebaseBackend secondary: StorageRepository
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)
}
