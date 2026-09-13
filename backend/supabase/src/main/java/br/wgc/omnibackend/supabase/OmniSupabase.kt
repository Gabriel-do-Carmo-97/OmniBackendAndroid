package br.wgc.omnibackend.supabase

import android.content.Context
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.supabase.data.repository.SupabaseAuthRepositoryImpl
import br.wgc.omnibackend.supabase.data.repository.SupabaseDatabaseRepositoryImpl
import br.wgc.omnibackend.supabase.data.repository.SupabaseStorageRepositoryImpl
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * Ponto de entrada e Fachada corporativa do driver Supabase para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` utilizando a infraestrutura do Supabase (GoTrue, PostgREST, Storage e Realtime).
 *
 * Exemplo de uso:
 * ```kotlin
 * // No Application.onCreate():
 * OmniSupabase.initialize(
 *     context = this,
 *     url = "https://seu-projeto.supabase.co",
 *     anonKey = "sua-anon-key"
 * )
 *
 * // Nos ViewModels ou UseCases:
 * val auth = OmniSupabase.auth
 * val userFlow = auth.authState
 * val database = OmniSupabase.database
 * ```
 */
object OmniSupabase {

    @Volatile
    private var isInitialized = false

    private lateinit var appContext: Context
    private lateinit var projectUrl: String
    private lateinit var projectAnonKey: String
    private lateinit var supabaseClient: SupabaseClient

    /**
     * Inicializa a configuração do cliente Supabase e seus subsistemas.
     *
     * @param context Contexto da aplicação Android.
     * @param url URL base do projeto no Supabase (ex: "https://xyz.supabase.co").
     * @param anonKey Chave pública anônima (`anon key`) do projeto Supabase.
     * @param sessionManager Gerenciador de sessão opcional (ex: MemorySessionManager para testes).
     * @param codeVerifierCache Cache de verificação PKCE opcional (ex: MemoryCodeVerifierCache para testes).
     */
    fun initialize(
        context: Context,
        url: String,
        anonKey: String,
        sessionManager: io.github.jan.supabase.auth.SessionManager? = null,
        codeVerifierCache: io.github.jan.supabase.auth.CodeVerifierCache? = null,
    ) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    appContext = context.applicationContext
                    projectUrl = url
                    projectAnonKey = anonKey
                    supabaseClient = createSupabaseClient(
                        supabaseUrl = url,
                        supabaseKey = anonKey,
                    ) {
                        install(Auth) {
                            sessionManager?.let { this.sessionManager = it }
                            codeVerifierCache?.let { this.codeVerifierCache = it }
                        }
                        install(Postgrest)
                        install(Storage)
                        install(Realtime)
                    }
                    isInitialized = true
                }
            }
        }
    }

    /**
     * Retorna `true` caso o SDK já tenha sido inicializado com sucesso.
     */
    val initialized: Boolean get() = isInitialized

    /**
     * URL do projeto Supabase configurada.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val url: String
        get() {
            check(isInitialized) { "OmniSupabase deve ser inicializado antes do uso. Chame OmniSupabase.initialize(...)" }
            return projectUrl
        }

    /**
     * Chave anônima pública configurada.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val anonKey: String
        get() {
            check(isInitialized) { "OmniSupabase deve ser inicializado antes do uso. Chame OmniSupabase.initialize(...)" }
            return projectAnonKey
        }

    /**
     * Instância interna do cliente Supabase.
     */
    internal val client: SupabaseClient
        get() {
            check(isInitialized) { "OmniSupabase deve ser inicializado antes do uso. Chame OmniSupabase.initialize(...)" }
            return supabaseClient
        }

    /**
     * Repositório de autenticação e controle de identidade (Supabase GoTrue).
     */
    val auth: AuthRepository by lazy {
        check(isInitialized) { "OmniSupabase deve ser inicializado antes do uso. Chame OmniSupabase.initialize(...)" }
        SupabaseAuthRepositoryImpl(supabaseClient.auth)
    }

    /**
     * Repositório de banco de dados NoSQL/relacional orientado a coleções e documentos (Supabase PostgREST & Realtime).
     */
    val database: FirestoreRepository by lazy {
        check(isInitialized) { "OmniSupabase deve ser inicializado antes do uso. Chame OmniSupabase.initialize(...)" }
        SupabaseDatabaseRepositoryImpl(
            postgrest = supabaseClient.postgrest,
            realtime = supabaseClient.realtime,
        )
    }

    /**
     * Repositório de armazenamento de arquivos em nuvem (Supabase Storage Buckets).
     */
    val storage: StorageRepository by lazy {
        check(isInitialized) { "OmniSupabase deve ser inicializado antes do uso. Chame OmniSupabase.initialize(...)" }
        SupabaseStorageRepositoryImpl(
            storage = supabaseClient.storage,
            context = appContext,
        )
    }
}
