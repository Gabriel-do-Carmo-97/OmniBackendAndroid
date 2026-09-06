package br.wgc.omnibackend.appwrite

import android.content.Context
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository

/**
 * Ponto de entrada e Fachada corporativa do driver Appwrite para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` utilizando o Appwrite Android SDK (`Account`, `Databases`, `Storage`).
 *
 * Exemplo de uso:
 * ```kotlin
 * // No Application.onCreate():
 * OmniAppwrite.initialize(
 *     context = this,
 *     endpoint = "https://cloud.appwrite.io/v1",
 *     projectId = "seu-project-id"
 * )
 *
 * // Nos ViewModels ou UseCases:
 * val auth = OmniAppwrite.auth
 * ```
 */
object OmniAppwrite {

    @Volatile
    private var isInitialized = false

    private lateinit var appwriteEndpoint: String
    private lateinit var appwriteProjectId: String

    /**
     * Inicializa a configuração do cliente Appwrite.
     *
     * @param context Contexto da aplicação Android.
     * @param endpoint URL do endpoint da API do Appwrite (ex: "https://cloud.appwrite.io/v1").
     * @param projectId Identificador único do projeto no Appwrite.
     */
    fun initialize(
        context: Context,
        endpoint: String,
        projectId: String
    ) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    appwriteEndpoint = endpoint
                    appwriteProjectId = projectId
                    isInitialized = true
                }
            }
        }
    }

    /** Retorna `true` caso o SDK já tenha sido inicializado. */
    val initialized: Boolean get() = isInitialized

    /**
     * Endpoint configurado da API do Appwrite.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val endpoint: String
        get() {
            check(isInitialized) { "OmniAppwrite deve ser inicializado antes do uso. Chame OmniAppwrite.initialize(...)" }
            return appwriteEndpoint
        }

    /**
     * Project ID configurado do Appwrite.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val projectId: String
        get() {
            check(isInitialized) { "OmniAppwrite deve ser inicializado antes do uso. Chame OmniAppwrite.initialize(...)" }
            return appwriteProjectId
        }
}
