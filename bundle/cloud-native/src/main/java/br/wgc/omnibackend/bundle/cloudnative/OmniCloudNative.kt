package br.wgc.omnibackend.bundle.cloudnative

import br.wgc.omnibackend.amplify.OmniAmplify
import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.firebase.OmniFirebase
import br.wgc.omnibackend.supabase.OmniSupabase

/**
 * Ponto de entrada corporativo para o bundle Cloud-Native (Grandes Provedores de Nuvem).
 *
 * Reúne os drivers dos maiores provedores globais de BaaS/Cloud:
 * - [OmniFirebase] (Google Cloud Platform)
 * - [OmniSupabase] (PostgreSQL / AWS)
 * - [OmniAmplify] (Amazon Web Services)
 * - [OmniHybrid] (Roteamento e failover multi-cloud entre GCP e AWS)
 */
object OmniCloudNative {
    /** Driver Google Firebase. */
    val firebase: OmniFirebase = OmniFirebase

    /** Driver Supabase (PostgreSQL). */
    val supabase: OmniSupabase = OmniSupabase

    /** Driver AWS Amplify. */
    val amplify: OmniAmplify = OmniAmplify

    /** Orquestrador de failover multi-cloud. */
    val hybrid: OmniHybrid = OmniHybrid
}
