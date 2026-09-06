package br.wgc.omnibackend.supabase

import android.content.Context
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository

/**
 * Ponto de entrada e Fachada corporativa do driver Supabase para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` utilizando a infraestrutura do Supabase (GoTrue, PostgREST, Storage).
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
 * ```
 */
object OmniSupabase {

    @Volatile
    private var isInitialized = false

    private lateinit var projectUrl: String
    private lateinit var projectAnonKey: String

    /**
     * Inicializa a configuração do cliente Supabase.
     *
     * @param context Contexto da aplicação Android.
     * @param url URL base do projeto no Supabase (ex: "https://xyz.supabase.co").
     * @param anonKey Chave pública anônima (`anon key`) do projeto Supabase.
     */
    fun initialize(
        context: Context,
        url: String,
        anonKey: String
    ) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    projectUrl = url
                    projectAnonKey = anonKey
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
}
