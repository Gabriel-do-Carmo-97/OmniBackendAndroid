package br.wgc.omnibackend.core.utils

/**
 * Define a hierarquia de erros agnóstica do ecossistema OmniBackend.
 * A estrutura aninhada de `sealed interface` organiza os erros por categoria,
 * permitindo tratamento explícito e unificado independente da nuvem utilizada.
 */
sealed interface AppError {

    /**
     * Erros genéricos de rede e operações inesperadas.
     */
    sealed interface Generic : AppError {
        object Network : Generic
        data class Unknown(val throwable: Throwable) : Generic
        data class GenericException(val msg: String) : Generic
    }

    /**
     * Erros de autenticação de usuários (Auth).
     */
    sealed interface Auth : AppError {
        object EmailAlreadyInUse : Auth
        object WeakPassword : Auth
        object InvalidCredentials : Auth
        object UserNotFound : Auth
        object InvalidActionCode : Auth
        object EmailSendFailed : Auth
        object RequiresRecentLogin : Auth
        object RecaptchaActivityMissing : Auth
        object WebOperationFailed : Auth
        data class Generic(val exception: Exception) : Auth
        data class MultiFactorRequired(val cause: Exception? = null) : Auth
    }

    /**
     * Erros relacionados a Bancos de Dados em Tempo Real.
     */
    sealed interface RealtimeDatabase : AppError {
        object PermissionDenied : RealtimeDatabase
        object DataStale : RealtimeDatabase
        object Disconnected : RealtimeDatabase
        object ExpiredToken : RealtimeDatabase
        object InvalidToken : RealtimeDatabase
        object MaxRetries : RealtimeDatabase
        object OverriddenBySet : RealtimeDatabase
        object Unavailable : RealtimeDatabase
        object WriteCanceled : RealtimeDatabase
        object OperationFailed : RealtimeDatabase
        data class Generic(val exception: Exception) : RealtimeDatabase
    }

    /**
     * Erros relacionados a Armazenamento de Arquivos (Storage / Buckets).
     */
    sealed interface Storage : AppError {
        object ObjectNotFound : Storage
        object QuotaExceeded : Storage
        object PermissionDenied : Storage
        object BucketNotFound : Storage
        object ProjectNotFound : Storage
        object UploadCancelled : Storage
        object DownloadFailed : Storage
        data class Generic(val exception: Exception) : Storage
    }

    /**
     * Erros relacionados a Bancos de Documentos (Firestore / NoSQL).
     */
    sealed interface Firestore : AppError {
        object PermissionDenied : Firestore
        object DocumentNotFound : Firestore
        object Aborted : Firestore
        data class Generic(val exception: Exception) : Firestore
    }

    /**
     * Erros relacionados a Configuração Remota (Remote Config).
     */
    sealed interface RemoteConfig : AppError {
        data object FetchFailure : RemoteConfig
        data object UpdateUnavailable : RemoteConfig
        data object StreamError : RemoteConfig
        data object MessageInvalid : RemoteConfig
        data object Unknown : RemoteConfig
    }

    /**
     * Erros relacionados a Mensagens In-App.
     */
    sealed interface InAppMessaging : AppError {
        object MessageDisplayError : InAppMessaging
        object ImageFetchFailed : InAppMessaging
        data class Generic(val exception: Exception) : InAppMessaging
    }

    /**
     * Erros relacionados a Notificações e Mensageria (Cloud Messaging / Push).
     */
    sealed interface Messaging : AppError {
        object TokenFetchFailed : Messaging
        object SendMessageFailed : Messaging
        data class Generic(val exception: Exception) : Messaging
    }

    /**
     * Erros de execução de Funções Serverless / Cloud Functions.
     */
    sealed interface Functions : AppError {
        object FunctionNotFound : Functions
        object Internal : Functions
        object Timeout : Functions
        data class Generic(val exception: Exception) : Functions
    }

    /**
     * Erros relacionados a APIs de IA Generativa (Gemini / Vertex AI).
     */
    sealed interface VertexAI : AppError {
        object ResponseBlocked : VertexAI
        object InvalidApiKey : VertexAI
        object QuotaExceeded : VertexAI
        object ModelUnavailable : VertexAI
        data class Generic(val exception: Exception) : VertexAI
    }
}
