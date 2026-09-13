package br.wgc.omnibackend.bundle.supabasecloudflare

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.cloudflare.OmniCloudflare
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.supabase.OmniSupabase

/**
 * Ponto de entrada corporativo para o bundle pareado Supabase + Cloudflare.
 *
 * Oferece união moderna entre PostgreSQL / GoTrue do Supabase e a computação Edge (Workers / D1 / R2) da Cloudflare.
 */
object OmniSupabaseCloudflare {

    /** Provedor primário: Supabase (PostgreSQL). */
    val supabase: OmniSupabase = OmniSupabase

    /** Provedor secundário: Cloudflare Edge. */
    val cloudflare: OmniCloudflare = OmniCloudflare

    /** Motor de failover híbrido agnóstico. */
    val hybrid: OmniHybrid = OmniHybrid

    /**
     * Cria repositório de autenticação híbrido com failover Supabase GoTrue -> Cloudflare Access.
     */
    fun createAuth(primary: AuthRepository = OmniSupabase.auth, secondary: AuthRepository = OmniCloudflare.auth): AuthRepository =
        OmniHybrid.createAuth(primary, secondary)

    /**
     * Cria repositório de banco de dados híbrido com failover Supabase PostgREST -> Cloudflare D1/KV.
     */
    fun createDatabase(
        primary: FirestoreRepository = OmniSupabase.database,
        secondary: FirestoreRepository = OmniCloudflare.database,
    ): FirestoreRepository = OmniHybrid.createDatabase(primary, secondary)

    /**
     * Cria repositório de armazenamento híbrido com failover Supabase Storage -> Cloudflare R2.
     */
    fun createStorage(
        primary: StorageRepository = OmniSupabase.storage,
        secondary: StorageRepository = OmniCloudflare.storage,
    ): StorageRepository = OmniHybrid.createStorage(primary, secondary)
}
