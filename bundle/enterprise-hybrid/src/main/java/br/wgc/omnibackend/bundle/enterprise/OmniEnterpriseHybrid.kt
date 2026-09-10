package br.wgc.omnibackend.bundle.enterprise

import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.firebase.OmniFirebase
import br.wgc.omnibackend.rest.OmniRestBackend

/**
 * Ponto de entrada corporativo para o bundle Enterprise-Hybrid.
 *
 * Projetado para empresas com APIs próprias/legadas (Spring Boot, Node.js, .NET, Go)
 * que desejam alta disponibilidade com failover automático para a nuvem do Google Firebase.
 */
object OmniEnterpriseHybrid {
    /** Driver de API REST corporativa proprietária. */
    val rest: OmniRestBackend = OmniRestBackend

    /** Driver Google Firebase (BaaS de contingência/suporte). */
    val firebase: OmniFirebase = OmniFirebase

    /** Orquestrador agnóstico de failover e roteamento híbrido. */
    val hybrid: OmniHybrid = OmniHybrid
}
