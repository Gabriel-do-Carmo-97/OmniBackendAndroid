package br.wgc.omnibackend.bundle.firebasesupabase

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.firebase.OmniFirebase
import br.wgc.omnibackend.supabase.OmniSupabase

/**
 * Ponto de entrada corporativo para o bundle pareado Firebase + Supabase.
 *
 * Oferece failover transparente ativo-passivo entre Google Firebase (primário)
 * e Supabase PostgreSQL (secundário/contingência).
 */
object OmniFirebaseSupabase {

    /** Provedor primário: Google Firebase. */
    val firebase: OmniFirebase = OmniFirebase

    /** Provedor secundário: Supabase (PostgreSQL). */
    val supabase: OmniSupabase = OmniSupabase

    /** Motor de failover híbrido agnóstico. */
    val hybrid: OmniHybrid = OmniHybrid

    /**
     * Cria repositório de autenticação híbrido com failover Firebase -> Supabase.
     */
    fun createAuth(primary: AuthRepository = OmniFirebase.auth, secondary: AuthRepository = OmniSupabase.auth): AuthRepository =
        OmniHybrid.createAuth(primary, secondary)

    /**
     * Cria repositório de banco de dados híbrido com failover Firestore -> PostgREST.
     */
    fun createDatabase(
        primary: FirestoreRepository = OmniFirebase.firestore,
        secondary: FirestoreRepository = OmniSupabase.database,
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Cria repositório de armazenamento híbrido com failover Firebase Storage -> Supabase Storage.
     */
    fun createStorage(
        primary: StorageRepository = OmniFirebase.storage,
        secondary: StorageRepository = OmniSupabase.storage,
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)
}
