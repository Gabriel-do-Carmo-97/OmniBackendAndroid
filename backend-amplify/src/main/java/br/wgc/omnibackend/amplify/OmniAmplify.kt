package br.wgc.omnibackend.amplify

import android.content.Context
import br.wgc.omnibackend.amplify.data.repository.AmplifyAuthRepositoryImpl
import br.wgc.omnibackend.amplify.data.repository.AmplifyDatabaseRepositoryImpl
import br.wgc.omnibackend.amplify.data.repository.AmplifyStorageRepositoryImpl
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
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
 * val db = OmniAmplify.database
 * val storage = OmniAmplify.storage
 * ```
 */
object OmniAmplify {

    @Volatile
    private var isInitialized = false
    private var appContext: Context? = null

    /**
     * Inicializa a configuração do AWS Amplify.
     *
     * @param context Contexto da aplicação Android.
     */
    fun initialize(context: Context) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    appContext = context.applicationContext
                    isInitialized = true
                }
            }
        }
    }

    /** Retorna `true` caso o SDK já tenha sido inicializado. */
    val initialized: Boolean get() = isInitialized

    /** Implementação de [AuthRepository] para AWS Cognito via Amplify. */
    val auth: AuthRepository by lazy {
        check(isInitialized) { "OmniAmplify deve ser inicializado antes do uso." }
        AmplifyAuthRepositoryImpl()
    }

    /** Implementação de [FirestoreRepository] para AWS DynamoDB/AppSync via Amplify. */
    val database: FirestoreRepository by lazy {
        check(isInitialized) { "OmniAmplify deve ser inicializado antes do uso." }
        AmplifyDatabaseRepositoryImpl()
    }

    /** Implementação de [StorageRepository] para AWS S3 via Amplify. */
    val storage: StorageRepository by lazy {
        check(isInitialized) { "OmniAmplify deve ser inicializado antes do uso." }
        AmplifyStorageRepositoryImpl(
            context = appContext ?: error("OmniAmplify deve ser inicializado antes do uso.")
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
