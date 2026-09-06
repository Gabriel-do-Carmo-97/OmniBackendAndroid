package br.wgc.omnibackend.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Contrato agnóstico para monitoramento reativo do status de conectividade de rede do dispositivo.
 */
interface NetworkMonitor {

    /**
     * Fluxo reativo que emite `true` quando o dispositivo possui conexão com a internet ativa
     * e `false` quando desconectado.
     */
    val isOnline: Flow<Boolean>
}
