package br.wgc.omnibackend.cloudflare

import android.content.Context
import br.wgc.omnibackend.core.repository.StorageRepository

/**
 * Ponto de entrada e Fachada corporativa do driver Cloudflare para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` consumindo serviços de borda da Cloudflare (Workers, R2, D1).
 *
 * Exemplo de uso:
 * ```kotlin
 * // No Application.onCreate():
 * OmniCloudflare.initialize(
 *     context = this,
 *     accountId = "seu-account-id",
 *     workerBaseUrl = "https://meu-worker.workers.dev"
 * )
 * ```
 */
object OmniCloudflare {

    @Volatile
    private var isInitialized = false

    private lateinit var cloudflareAccountId: String
    private lateinit var cloudflareWorkerUrl: String

    /**
     * Inicializa a configuração dos serviços Cloudflare.
     *
     * @param context Contexto da aplicação Android.
     * @param accountId Identificador da conta na Cloudflare.
     * @param workerBaseUrl URL base do Cloudflare Worker (ex: "https://meu-worker.workers.dev").
     */
    fun initialize(
        context: Context,
        accountId: String,
        workerBaseUrl: String
    ) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    cloudflareAccountId = accountId
                    cloudflareWorkerUrl = workerBaseUrl
                    isInitialized = true
                }
            }
        }
    }

    /** Retorna `true` caso o SDK já tenha sido inicializado. */
    val initialized: Boolean get() = isInitialized

    /**
     * Account ID da Cloudflare configurado.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val accountId: String
        get() {
            check(isInitialized) { "OmniCloudflare deve ser inicializado antes do uso. Chame OmniCloudflare.initialize(...)" }
            return cloudflareAccountId
        }

    /**
     * URL do Worker Cloudflare configurada.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val workerBaseUrl: String
        get() {
            check(isInitialized) { "OmniCloudflare deve ser inicializado antes do uso. Chame OmniCloudflare.initialize(...)" }
            return cloudflareWorkerUrl
        }
}
