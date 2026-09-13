package br.wgc.omnibackend.bundle.firebaseamplify

import br.wgc.omnibackend.amplify.OmniAmplify
import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.firebase.OmniFirebase

/**
 * Ponto de entrada corporativo para o bundle pareado Firebase + AWS Amplify (GCP + AWS).
 *
 * Oferece redundância multi-cloud corporativa direta entre os dois maiores provedores do mercado.
 */
object OmniFirebaseAmplify {

    /** Provedor GCP: Google Firebase. */
    val firebase: OmniFirebase = OmniFirebase

    /** Provedor AWS: Amazon Web Services Amplify. */
    val amplify: OmniAmplify = OmniAmplify

    /** Motor de failover híbrido agnóstico. */
    val hybrid: OmniHybrid = OmniHybrid

    /**
     * Cria repositório de autenticação híbrido com failover Firebase Auth -> AWS Cognito.
     */
    fun createAuth(primary: AuthRepository = OmniFirebase.auth, secondary: AuthRepository = OmniAmplify.auth): AuthRepository =
        OmniHybrid.createAuth(primary, secondary)

    /**
     * Cria repositório de banco de dados híbrido com failover Firestore -> AWS AppSync/DynamoDB.
     */
    fun createDatabase(
        primary: FirestoreRepository = OmniFirebase.firestore,
        secondary: FirestoreRepository = OmniAmplify.database,
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Cria repositório de armazenamento híbrido com failover Firebase Storage -> Amazon S3.
     */
    fun createStorage(
        primary: StorageRepository = OmniFirebase.storage,
        secondary: StorageRepository = OmniAmplify.storage,
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)
}
