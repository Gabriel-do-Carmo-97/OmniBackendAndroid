package br.wgc.omnibackend.core.network

import br.wgc.omnibackend.core.telemetry.TraceContext
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

/**
 * Interceptor para Observabilidade e Tracing Distribuído compatível com OpenTelemetry / W3C Trace Context.
 *
 * Injeta cabeçalhos corporativos de rastreamento em todas as chamadas HTTP:
 * - `X-Request-ID`: Identificador único da requisição (UUIDv4).
 * - `traceparent`: Especificação oficial W3C Trace Context (versão 00, 16-byte traceId, 8-byte spanId, sample flag 01).
 */
class W3CTraceInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val correlationId = originalRequest.header(HEADER_REQUEST_ID)
            ?: UUID.randomUUID().toString()
        requestBuilder.header(HEADER_REQUEST_ID, correlationId)

        val traceparent = originalRequest.header(HEADER_TRACEPARENT)
            ?: TraceContext.createTraceParent()
        requestBuilder.header(HEADER_TRACEPARENT, traceparent)

        return chain.proceed(requestBuilder.build())
    }

    companion object {
        const val HEADER_REQUEST_ID = "X-Request-ID"
        const val HEADER_TRACEPARENT = "traceparent"
        private const val TRACE_ID_BYTE_COUNT = 16
        private const val SPAN_ID_BYTE_COUNT = 8
    }
}
