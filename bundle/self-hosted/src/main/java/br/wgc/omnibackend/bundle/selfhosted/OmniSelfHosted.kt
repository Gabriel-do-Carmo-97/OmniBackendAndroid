package br.wgc.omnibackend.bundle.selfhosted

import br.wgc.omnibackend.appwrite.OmniAppwrite
import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import br.wgc.omnibackend.pocketbase.OmniPocketBase

/**
 * Ponto de entrada para o bundle Self-Hosted (Backends Independentes / Auto-Hospedados).
 *
 * Fornece acesso ao [OmniPocketBase], [OmniAppwrite] e [OmniHybrid] para redundância
 * entre infraestruturas privadas e VPS de baixo custo sem dependência de Big Techs.
 */
object OmniSelfHosted {
    /** Driver do PocketBase (SQLite / Go). */
    val pocketBase: OmniPocketBase = OmniPocketBase

    /** Driver do Appwrite (Docker / Self-Hosted / Cloud). */
    val appwrite: OmniAppwrite = OmniAppwrite

    /** Orquestrador de failover entre backends auto-hospedados. */
    val hybrid: OmniHybrid = OmniHybrid
}
