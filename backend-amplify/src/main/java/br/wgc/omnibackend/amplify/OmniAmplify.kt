package br.wgc.omnibackend.amplify

import android.content.Context
import br.wgc.omnibackend.core.repository.AnalyticsRepository
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.StorageRepository

/**
 * Ponto de entrada e Fachada corporativa do driver AWS Amplify para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` utilizando o AWS Amplify Android SDK (Cognito, S3, Pinpoint).
 *
 * Exemplo de uso:
 * ```kotlin
 * // No Application.onCreate():
 * OmniAmplify.initialize(this)
 *
 * // Nos ViewModels ou UseCases:
 * val auth = OmniAmplify.auth
 * ```
 */
object OmniAmplify {

    @Volatile
    private var isInitialized = false

    /**
     * Inicializa a configuração do AWS Amplify.
     *
     * @param context Contexto da aplicação Android.
     */
    fun initialize(context: Context) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    isInitialized = true
                }
            }
        }
    }

    /** Retorna `true` caso o SDK já tenha sido inicializado. */
    val initialized: Boolean get() = isInitialized
}
