package br.wgc.omnibackend.bundle.enterprise.di

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.core.di.FirebaseBackend
import br.wgc.omnibackend.core.di.RestBackend
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
 * Módulo Hilt corporativo para o bundle Enterprise-Hybrid (:bundle:enterprise-hybrid).
 *
 * Provê instâncias integrando APIs REST corporativas proprietárias com Google Firebase
 * para cenários de contingência e failover ativo-passivo empresarial.
 */
@Module
@InstallIn(SingletonComponent::class)
object EnterpriseHybridBackendModule {

    /** Constante de qualificação por nome para failover REST (primário) + Firebase (secundário). */
    const val QUALIFIER_FAILOVER = "enterprise_failover"

    /**
     * Provê autenticação com failover entre API REST própria e Firebase.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideEnterpriseFailoverAuth(@RestBackend primary: AuthRepository, @FirebaseBackend secondary: AuthRepository): AuthRepository =
        OmniHybrid.createAuth(primary, secondary)

    /**
     * Provê banco de dados com failover entre API REST própria e Firebase Firestore.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideEnterpriseFailoverDatabase(
        @RestBackend primary: FirestoreRepository,
        @FirebaseBackend secondary: FirestoreRepository,
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Provê armazenamento com failover entre API REST própria e Firebase Storage.
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER)
    fun provideEnterpriseFailoverStorage(
        @RestBackend primary: StorageRepository,
        @FirebaseBackend secondary: StorageRepository,
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)
}
