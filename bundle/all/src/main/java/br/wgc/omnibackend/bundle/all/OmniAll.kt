package br.wgc.omnibackend.bundle.all

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid

/**
 * Ponto de entrada corporativo para o bundle completo do OmniBackend.
 *
 * Expõe todos os provedores de backend e capacidades de orquestração híbrida
 * em um único artefato.
 */
object OmniAll {
    /** Acesso rápido ao orquestrador híbrido com suporte a failover dinâmico. */
    val hybrid: OmniHybrid = OmniHybrid
}
