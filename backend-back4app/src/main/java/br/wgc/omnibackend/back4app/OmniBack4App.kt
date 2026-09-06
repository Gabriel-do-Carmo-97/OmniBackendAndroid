package br.wgc.omnibackend.back4app

import android.content.Context
import br.wgc.omnibackend.back4app.data.repository.Back4AppAuthRepositoryImpl
import br.wgc.omnibackend.back4app.data.repository.Back4AppDatabaseRepositoryImpl
import br.wgc.omnibackend.back4app.data.repository.Back4AppStorageRepositoryImpl
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import com.parse.Parse

/**
 * Ponto de entrada e Fachada corporativa do driver Back4App (Parse Platform) para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` utilizando o Parse Android SDK (`ParseUser`, `ParseObject`).
 *
 * Exemplo de uso:
 * ```kotlin
 * // No Application.onCreate():
 * OmniBack4App.initialize(
 *     context = this,
 *     appId = "seu-app-id",
 *     clientKey = "sua-client-key"
 * )
 *
 * val auth = OmniBack4App.auth
 * val db = OmniBack4App.database
 * val storage = OmniBack4App.storage
 * ```
 */
object OmniBack4App {

    @Volatile
    private var isInitialized = false

    private lateinit var parseAppId: String
    private lateinit var parseClientKey: String
    private var parseServerUrl: String = "https://parseapi.back4app.com"
    private var appContext: Context? = null

    /**
     * Inicializa a configuração do cliente Parse / Back4App.
     *
     * @param context Contexto da aplicação Android.
     * @param appId Application ID gerado no painel do Back4App.
     * @param clientKey Client Key pública gerada no painel do Back4App.
     * @param serverUrl URL base do servidor Parse (padrão: "https://parseapi.back4app.com").
     */
    fun initialize(
        context: Context,
        appId: String,
        clientKey: String,
        serverUrl: String = "https://parseapi.back4app.com"
    ) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    appContext = context.applicationContext
                    parseAppId = appId
                    parseClientKey = clientKey
                    parseServerUrl = serverUrl

                    val config = Parse.Configuration.Builder(context.applicationContext)
                        .applicationId(appId)
                        .clientKey(clientKey)
                        .server(serverUrl)
                        .build()
                    Parse.initialize(config)

                    isInitialized = true
                }
            }
        }
    }

    /** Retorna `true` caso o SDK já tenha sido inicializado. */
    val initialized: Boolean get() = isInitialized

    /**
     * Application ID configurado.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val appId: String
        get() {
            check(isInitialized) { "OmniBack4App deve ser inicializado antes do uso. Chame OmniBack4App.initialize(...)" }
            return parseAppId
        }

    /**
     * Client Key configurada.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val clientKey: String
        get() {
            check(isInitialized) { "OmniBack4App deve ser inicializado antes do uso. Chame OmniBack4App.initialize(...)" }
            return parseClientKey
        }

    /** URL do servidor Parse / Back4App configurada. */
    val serverUrl: String get() = parseServerUrl

    /** Implementação de [AuthRepository] para autenticação via Back4App (ParseUser). */
    val auth: AuthRepository by lazy {
        check(isInitialized) { "OmniBack4App deve ser inicializado antes do uso." }
        Back4AppAuthRepositoryImpl()
    }

    /** Implementação de [FirestoreRepository] para banco de dados via Back4App (ParseObject). */
    val database: FirestoreRepository by lazy {
        check(isInitialized) { "OmniBack4App deve ser inicializado antes do uso." }
        Back4AppDatabaseRepositoryImpl()
    }

    /** Implementação de [StorageRepository] para arquivos via Back4App (ParseFile). */
    val storage: StorageRepository by lazy {
        check(isInitialized) { "OmniBack4App deve ser inicializado antes do uso." }
        Back4AppStorageRepositoryImpl(
            context = appContext ?: error("OmniBack4App deve ser inicializado antes do uso.")
        )
    }

    /** Reinicia o estado singleton para testes unitários. */
    internal fun resetForTesting() {
        synchronized(this) {
            isInitialized = false
            appContext = null
        }
    }
}
