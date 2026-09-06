package br.wgc.omnibackend.rest

import android.content.Context
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.rest.data.repository.RestAuthRepositoryImpl
import br.wgc.omnibackend.rest.data.repository.RestDatabaseRepositoryImpl
import br.wgc.omnibackend.rest.data.repository.RestStorageRepositoryImpl

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
 *
 * val auth = OmniRestBackend.auth
 * val db = OmniRestBackend.database
 * val storage = OmniRestBackend.storage
 * ```
 */
object OmniRestBackend {

    @Volatile
    private var isInitialized = false

    private lateinit var restBaseUrl: String
    private var appContext: Context? = null

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
                    appContext = context.applicationContext
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

    /** Implementação de [AuthRepository] para API REST customizada. */
    val auth: AuthRepository by lazy {
        check(isInitialized) { "OmniRestBackend deve ser inicializado antes do uso." }
        RestAuthRepositoryImpl(restBaseUrl)
    }

    /** Implementação de [FirestoreRepository] para API REST customizada. */
    val database: FirestoreRepository by lazy {
        check(isInitialized) { "OmniRestBackend deve ser inicializado antes do uso." }
        RestDatabaseRepositoryImpl(restBaseUrl)
    }

    /** Implementação de [StorageRepository] para API REST customizada. */
    val storage: StorageRepository by lazy {
        check(isInitialized) { "OmniRestBackend deve ser inicializado antes do uso." }
        RestStorageRepositoryImpl(
            context = appContext ?: error("OmniRestBackend deve ser inicializado antes do uso."),
            baseUrl = restBaseUrl
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
