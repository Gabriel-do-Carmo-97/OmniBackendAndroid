package br.wgc.omnibackend.core.network

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Fábrica centralizada de instâncias [OkHttpClient] corporativas pré-configuradas para o OmniBackend.
 *
 * Configurações padrão industriais:
 * - Pool de conexões (5 conexões ociosas, keep-alive de 5 minutos).
 * - Timeouts calibrados (15s connect, 30s read, 30s write).
 * - [IdempotencyInterceptor] para proteção de mutações duplicadas.
 * - [W3CTraceInterceptor] para rastreabilidade distribuída OpenTelemetry.
 * - Suporte opcional a injeção de Bearer Token e renovação transparente com [TokenAuthenticator].
 */
object OmniHttpClientFactory {

    private const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 15L
    private const val DEFAULT_READ_TIMEOUT_SECONDS = 30L
    private const val DEFAULT_WRITE_TIMEOUT_SECONDS = 30L
    private const val POOL_MAX_IDLE_CONNECTIONS = 5
    private const val POOL_KEEP_ALIVE_DURATION_MINUTES = 5L

    /**
     * Cria uma instância otimizada e blindada de [OkHttpClient].
     */
    fun createClient(
        tokenProvider: TokenProvider? = null,
        interceptors: List<Interceptor> = emptyList(),
        connectTimeoutSeconds: Long = DEFAULT_CONNECT_TIMEOUT_SECONDS,
        readTimeoutSeconds: Long = DEFAULT_READ_TIMEOUT_SECONDS,
        writeTimeoutSeconds: Long = DEFAULT_WRITE_TIMEOUT_SECONDS,
        enableLogging: Boolean = false,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectionPool(
                ConnectionPool(
                    POOL_MAX_IDLE_CONNECTIONS,
                    POOL_KEEP_ALIVE_DURATION_MINUTES,
                    TimeUnit.MINUTES,
                ),
            )
            .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .addInterceptor(IdempotencyInterceptor())
            .addInterceptor(W3CTraceInterceptor())

        interceptors.forEach { builder.addInterceptor(it) }

        if (tokenProvider != null) {
            builder.addInterceptor(AuthBearerInterceptor(tokenProvider))
            builder.authenticator(TokenAuthenticator(tokenProvider))
        }

        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }
}
