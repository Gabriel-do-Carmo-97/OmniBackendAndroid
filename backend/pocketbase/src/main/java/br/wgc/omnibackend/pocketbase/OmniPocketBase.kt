package br.wgc.omnibackend.pocketbase

import android.content.Context
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.pocketbase.data.repository.PocketBaseAuthRepositoryImpl
import br.wgc.omnibackend.pocketbase.data.repository.PocketBaseDatabaseRepositoryImpl
import br.wgc.omnibackend.pocketbase.data.repository.PocketBaseStorageRepositoryImpl

/**
 * Ponto de entrada e Fachada corporativa do driver PocketBase para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` utilizando o PocketBase (Go/SQLite, RecordAuth, Realtime SSE e Files).
 *
 * Exemplo de uso:
 * ```kotlin
 * // No Application.onCreate():
 * OmniPocketBase.initialize(
 *     context = this,
 *     baseUrl = "https://seu-pocketbase.app"
 * )
 *
 * val auth = OmniPocketBase.auth
 * val db = OmniPocketBase.database
 * val storage = OmniPocketBase.storage
 * ```
 */
object OmniPocketBase {

    @Volatile
    private var isInitialized = false

    private lateinit var pocketBaseUrl: String
    private var appContext: Context? = null

    /**
     * Inicializa a configuração do PocketBase.
     *
     * @param context Contexto da aplicação Android.
     * @param baseUrl URL base da instância do PocketBase (ex: "https://seu-pocketbase.app").
     */
    fun initialize(context: Context, baseUrl: String) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    appContext = context.applicationContext
                    pocketBaseUrl = baseUrl
                    isInitialized = true
                }
            }
        }
    }

    /** Retorna `true` caso o SDK já tenha sido inicializado. */
    val initialized: Boolean get() = isInitialized

    /**
     * URL base do servidor PocketBase configurada.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val baseUrl: String
        get() {
            check(isInitialized) { "OmniPocketBase deve ser inicializado antes do uso. Chame OmniPocketBase.initialize(...)" }
            return pocketBaseUrl
        }

    /** Implementação de [AuthRepository] para PocketBase (RecordAuth). */
    val auth: AuthRepository by lazy {
        check(isInitialized) { "OmniPocketBase deve ser inicializado antes do uso." }
        PocketBaseAuthRepositoryImpl(pocketBaseUrl)
    }

    /** Implementação de [FirestoreRepository] para banco de dados PocketBase. */
    val database: FirestoreRepository by lazy {
        check(isInitialized) { "OmniPocketBase deve ser inicializado antes do uso." }
        PocketBaseDatabaseRepositoryImpl(pocketBaseUrl)
    }

    /** Implementação de [StorageRepository] para arquivos PocketBase. */
    val storage: StorageRepository by lazy {
        check(isInitialized) { "OmniPocketBase deve ser inicializado antes do uso." }
        PocketBaseStorageRepositoryImpl(
            context = appContext ?: error("OmniPocketBase deve ser inicializado antes do uso."),
            baseUrl = pocketBaseUrl,
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
