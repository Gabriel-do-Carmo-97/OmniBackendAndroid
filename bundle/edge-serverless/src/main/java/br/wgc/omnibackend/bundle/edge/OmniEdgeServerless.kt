package br.wgc.omnibackend.bundle.edge

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.cloudflare.OmniCloudflare
import br.wgc.omnibackend.supabase.OmniSupabase

/**
 * Ponto de entrada corporativo para o bundle Edge-Serverless.
 *
 * Combina o poder de computação de borda de ultra-baixa latência da Cloudflare
 * (Workers API, D1 Database, R2 Object Storage) com a robustez relacional do Supabase (PostgreSQL).
 */
object OmniEdgeServerless {
    /** Driver Cloudflare Edge (Workers, D1, R2). */
    val cloudflare: OmniCloudflare = OmniCloudflare

    /** Driver Supabase (PostgreSQL, Realtime, Storage). */
    val supabase: OmniSupabase = OmniSupabase

    /** Orquestrador agnóstico de failover e balanceamento. */
    val hybrid: OmniHybrid = OmniHybrid
}
