package br.wgc.omnibackend.core.telemetry

import br.wgc.omnibackend.core.utils.AppError

/**
 * Contrato de observabilidade e captura de erros para provedores de nuvem.
 */
interface TelemetryProvider {
    fun recordError(error: AppError, attributes: Map<String, Any> = emptyMap())
    fun recordException(throwable: Throwable, attributes: Map<String, Any> = emptyMap())
    fun log(message: String)
    fun setUserId(userId: String)
    fun setAttribute(key: String, value: Any)
}
