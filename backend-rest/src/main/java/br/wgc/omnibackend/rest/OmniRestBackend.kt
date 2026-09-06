package br.wgc.omnibackend.rest

import android.content.Context
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository

/**
 * Ponto de entrada e Fachada corporativa do driver Custom REST para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` consumindo APIs REST e microserviços corporativos proprietários.
 *
 * Exemplo de uso:
 * ```kotlin
 * // No Application.onCreate():
 * OmniRestBackend.initialize(
 *     context = this,
 *     baseUrl = "https://api.empresa.com"
 * )
 * ```
 */
object OmniRestBackend {

    @Volatile
    private var isInitialized = false

    private lateinit var restBaseUrl: String

    /**
     * Inicializa a configuração do backend REST customizado.
     *
     * @param context Contexto da aplicação Android.
     * @param baseUrl URL base da API REST corporativa (ex: "https://api.empresa.com").
     */
    fun initialize(
        context: Context,
        baseUrl: String
    ) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    restBaseUrl = baseUrl
                    isInitialized = true
                }
            }
        }
    }

    /** Retorna `true` caso o SDK já tenha sido inicializado. */
    val initialized: Boolean get() = isInitialized

    /**
     * URL base configurada da API REST.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val baseUrl: String
        get() {
            check(isInitialized) { "OmniRestBackend deve ser inicializado antes do uso. Chame OmniRestBackend.initialize(...)" }
            return restBaseUrl
        }
}
