package br.wgc.omnibackend.bundle.classic

import br.wgc.omnibackend.back4app.OmniBack4App
import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.firebase.OmniFirebase

/**
 * Ponto de entrada corporativo para o bundle BaaS-Classic.
 *
 * Focado na união entre a maturidade relacional do Parse Platform ([OmniBack4App])
 * e a infraestrutura em escala do Google Cloud ([OmniFirebase]), permitindo migrações
 * seguras ou tolerância a falhas (ativo-passivo).
 */
object OmniBaaSClassic {
    /** Driver Back4App / Parse Platform. */
    val back4App: OmniBack4App = OmniBack4App

    /** Driver Google Firebase. */
    val firebase: OmniFirebase = OmniFirebase

    /** Orquestrador agnóstico de failover híbrido. */
    val hybrid: OmniHybrid = OmniHybrid
}
