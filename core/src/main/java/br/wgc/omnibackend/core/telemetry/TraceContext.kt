package br.wgc.omnibackend.core.telemetry

import java.util.UUID

/**
 * Utilitário corporativo para geração e manipulação de W3C Trace Context (`traceparent`).
 *
 * O formato de cabeçalho `traceparent` segue a especificação W3C:
 * `00-{traceId}-{spanId}-01`
 * - `version`: `00`
 * - `traceId`: Hexadecimal de 32 caracteres (128 bits)
 * - `spanId`: Hexadecimal de 16 caracteres (64 bits)
 * - `traceFlags`: `01` (SAMPLED)
 */
object TraceContext {

    /**
     * Gera um novo identificador `traceparent` no padrão W3C.
     *
     * @return String formatada contendo `00-{traceId}-{spanId}-01`.
     */
    fun createTraceParent(): String {
        val traceId = UUID.randomUUID().toString().replace("-", "")
        val spanId = UUID.randomUUID().toString().replace("-", "").take(16)
        return "00-$traceId-$spanId-01"
    }

    /**
     * Extrai o `traceId` a partir de uma String `traceparent` W3C válida.
     *
     * @param traceParent String `traceparent` recebida.
     * @return O `traceId` hexadecimal de 32 caracteres, ou `null` se inválido.
     */
    fun extractTraceId(traceParent: String): String? {
        val parts = traceParent.split("-")
        return if (parts.size == 4 && parts[0] == "00" && parts[1].length == 32) {
            parts[1]
        } else null
    }
}
