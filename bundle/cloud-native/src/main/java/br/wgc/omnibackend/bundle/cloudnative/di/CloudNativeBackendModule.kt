package br.wgc.omnibackend.bundle.cloudnative.di

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.core.di.ActiveBackend
import br.wgc.omnibackend.core.di.AmplifyBackend
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
 * Módulo Hilt corporativo para o bundle Cloud-Native (:bundle:cloud-native).
 *
 * Provê instâncias integradas dos maiores provedores de nuvem (Firebase, Supabase e AWS Amplify)
 * com suporte a failover e resolução dinâmica via [OmniBackendSelector].
 */
@Module
@InstallIn(SingletonComponent::class)
object CloudNativeBackendModule {

    /** Constante de qualificação por nome para failover GCP (Firebase) + AWS (Amplify). */
    const val QUALIFIER_FAILOVER_AWS = "cloud_native_gcp_aws"

    /** Constante de qualificação por nome para failover GCP (Firebase) + Supabase (Postgres). */
    const val QUALIFIER_FAILOVER_SUPABASE = "cloud_native_gcp_supabase"

    /**
     * Provê autenticação híbrida com failover entre Firebase (primário) e AWS Cognito via Amplify (secundário).
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER_AWS)
    fun provideGcpAwsFailoverAuth(
        @FirebaseBackend primary: AuthRepository,
        @AmplifyBackend secondary: AuthRepository
    ): AuthRepository = OmniHybrid.createAuth(primary, secondary)

    /**
     * Provê armazenamento híbrido com failover entre Firebase Storage (primário) e AWS S3 via Amplify (secundário).
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER_AWS)
    fun provideGcpAwsFailoverStorage(
        @FirebaseBackend primary: StorageRepository,
        @AmplifyBackend secondary: StorageRepository
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)

    /**
     * Provê banco de dados com failover entre Firebase Firestore (primário) e Supabase PostgREST (secundário).
     */
    @Provides
    @Singleton
    @Named(QUALIFIER_FAILOVER_SUPABASE)
    fun provideGcpSupabaseFailoverDatabase(
        @FirebaseBackend primary: FirestoreRepository,
        @SupabaseBackend secondary: FirestoreRepository
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)
}
