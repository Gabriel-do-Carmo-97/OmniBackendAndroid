package br.wgc.omnibackend.cloudflare

import android.content.Context
import br.wgc.omnibackend.cloudflare.data.repository.CloudflareAuthRepositoryImpl
import br.wgc.omnibackend.cloudflare.data.repository.CloudflareDatabaseRepositoryImpl
import br.wgc.omnibackend.cloudflare.data.repository.CloudflareStorageRepositoryImpl
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
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
 *
 * val auth = OmniCloudflare.auth
 * val db = OmniCloudflare.database
 * val storage = OmniCloudflare.storage
 * ```
 */
object OmniCloudflare {

    @Volatile
    private var isInitialized = false

    private lateinit var cloudflareAccountId: String
    private lateinit var cloudflareWorkerUrl: String
    private var appContext: Context? = null

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
                    appContext = context.applicationContext
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

    /** Implementação de [AuthRepository] para Cloudflare (Workers Auth). */
    val auth: AuthRepository by lazy {
        check(isInitialized) { "OmniCloudflare deve ser inicializado antes do uso." }
        CloudflareAuthRepositoryImpl(cloudflareWorkerUrl)
    }

    /** Implementação de [FirestoreRepository] para Cloudflare D1. */
    val database: FirestoreRepository by lazy {
        check(isInitialized) { "OmniCloudflare deve ser inicializado antes do uso." }
        CloudflareDatabaseRepositoryImpl(cloudflareWorkerUrl)
    }

    /** Implementação de [StorageRepository] para Cloudflare R2. */
    val storage: StorageRepository by lazy {
        check(isInitialized) { "OmniCloudflare deve ser inicializado antes do uso." }
        CloudflareStorageRepositoryImpl(
            context = appContext ?: error("OmniCloudflare deve ser inicializado antes do uso."),
            workerBaseUrl = cloudflareWorkerUrl
        )
    }

    /** Reinicia estado singleton para testes. */
    internal fun resetForTesting() {
        synchronized(this) {
            isInitialized = false
            appContext = null
        }
    }
}
