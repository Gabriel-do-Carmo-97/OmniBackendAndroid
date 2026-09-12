package br.wgc.omnibackend.bundle.firebaseback4app

import br.wgc.omnibackend.back4app.OmniBack4App
import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.firebase.OmniFirebase

/**
 * Ponto de entrada corporativo para o bundle pareado Firebase + Back4App (Parse Platform).
 *
 * Oferece failover e contingência entre Google Firebase e a infraestrutura Parse do Back4App.
 */
object OmniFirebaseBack4App {

    /** Provedor primário: Google Firebase. */
    val firebase: OmniFirebase = OmniFirebase

    /** Provedor secundário: Back4App (Parse Platform). */
    val back4app: OmniBack4App = OmniBack4App

    /** Motor de failover híbrido agnóstico. */
    val hybrid: OmniHybrid = OmniHybrid

    /**
     * Cria repositório de autenticação híbrido Firebase Auth -> Back4App ParseUser.
     */
    fun createAuth(
        primary: AuthRepository = OmniFirebase.auth,
        secondary: AuthRepository = OmniBack4App.auth
    ): AuthRepository = OmniHybrid.createAuth(primary, secondary)

    /**
     * Cria repositório de banco de dados híbrido Firestore -> Back4App ParseObject.
     */
    fun createDatabase(
        primary: FirestoreRepository = OmniFirebase.firestore,
        secondary: FirestoreRepository = OmniBack4App.database
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Cria repositório de armazenamento híbrido Firebase Storage -> Back4App ParseFile.
     */
    fun createStorage(
        primary: StorageRepository = OmniFirebase.storage,
        secondary: StorageRepository = OmniBack4App.storage
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)
}
