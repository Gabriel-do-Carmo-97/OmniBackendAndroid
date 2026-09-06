package br.wgc.omnibackend.firebase.utils

import com.google.firebase.auth.FirebaseAuthMultiFactorException

/**
 * Define a hierarquia de erros tratados na aplicação.
 * A estrutura aninhada de `sealed interface` organiza os erros por categoria,
 * tornando o código mais limpo e o tratamento de erros mais explícito.
 */
sealed interface AppError {

    /**
     * Erros que não pertencem a um recurso específico do Firebase.
     */
    sealed interface Generic : AppError {
        /**
         * Indica que a operação falhou por falta de conexão com a internet.
         */
        object Network : Generic

        /**
         * Representa um erro inesperado ou não categorizado.
         * @param throwable A exceção original que causou o erro, para fins de depuração.
         */
        data class Unknown(val throwable: Throwable) : Generic
        data class GenericException(val msg: String) : Generic


    }

    /**
     * Erros relacionados à autenticação de usuários com o Firebase Auth.
     */
    sealed interface Auth : AppError {
        /** O email fornecido já está em uso por outra conta. */
        object EmailAlreadyInUse : Auth
        /** A senha fornecida é muito fraca e não atende aos requisitos de segurança. */
        object WeakPassword : Auth
        /** As credenciais (email/senha) fornecidas são inválidas. */
        object InvalidCredentials : Auth
        /** A conta de usuário não foi encontrada. */
        object UserNotFound : Auth
        /** O código de ação (ex: para reset de senha) é inválido ou já expirou. */
        object InvalidActionCode : Auth
        /** Falha ao enviar um email (ex: verificação de email ou reset de senha). */
        object EmailSendFailed : Auth
        /** A operação solicitada é sensível e requer que o usuário tenha feito login recentemente. */
        object RequiresRecentLogin : Auth
        /** A verificação reCAPTCHA é necessária, mas a Activity não foi configurada. */
        object RecaptchaActivityMissing : Auth
        /** Ocorreu um erro durante uma operação web, como um fluxo OAuth. */
        object WebOperationFailed : Auth

        /**
         * Representa um erro genérico do Firebase Auth não coberto pelos casos específicos.
         * @param exception A exceção original do Firebase. Útil para logging.
         */
        data class Generic(val exception: Exception) : Auth

        /**
         * A autenticação requer um segundo fator.
         * @param resolver Contém as informações necessárias para que a UI continue o fluxo de MFA.
         */
        data class MultiFactorRequired(val resolver: FirebaseAuthMultiFactorException) : Auth
    }

    /**
     * Erros relacionados ao Firebase Realtime Database.
     */
    sealed interface RealtimeDatabase : AppError {
        /** O cliente não tem permissão para ler ou escrever nos dados solicitados. */
        object PermissionDenied : RealtimeDatabase
        /** A operação falhou porque os dados no cliente estavam desatualizados (conflito de transação). */
        object DataStale : RealtimeDatabase
        /** O cliente está desconectado do servidor do Realtime Database. */
        object Disconnected : RealtimeDatabase
        /** O token de autenticação do cliente expirou. */
        object ExpiredToken : RealtimeDatabase
        /** O token de autenticação do cliente é inválido. */
        object InvalidToken : RealtimeDatabase
        /** A operação falhou após o número máximo de novas tentativas. */
        object MaxRetries : RealtimeDatabase
        /** A operação foi substituída por uma operação de `set` no mesmo local. */
        object OverriddenBySet : RealtimeDatabase
        /** O serviço não está disponível no momento. */
        object Unavailable : RealtimeDatabase
        /** A operação de escrita foi cancelada pelo usuário. */
        object WriteCanceled : RealtimeDatabase
        /** A operação falhou por um motivo não especificado. */
        object OperationFailed : RealtimeDatabase
        /**
         * Representa um erro genérico do Realtime Database.
         * @param exception A exceção original do Firebase.
         */
        data class Generic(val exception: Exception) : RealtimeDatabase
    }

    /**
     * Erros relacionados ao Cloud Storage for Firebase.
     */
    sealed interface Storage : AppError {
        /** O arquivo ou objeto solicitado não foi encontrado no bucket. */
        object ObjectNotFound : Storage
        /** A cota de uso do Cloud Storage foi excedida. */
        object QuotaExceeded : Storage
        /** O cliente não tem permissão para acessar ou modificar o objeto. */
        object PermissionDenied : Storage
        /** O bucket do Cloud Storage especificado não foi encontrado. */
        object BucketNotFound : Storage
        /** O projeto do Cloud Storage especificado não foi encontrado. */
        object ProjectNotFound : Storage
        /** A operação foi cancelada, geralmente pelo usuário. */
        object UploadCancelled : Storage
        /** A transferência falhou devido a uma soma de verificação inválida, indicando corrupção de dados. */
        object DownloadFailed : Storage
        /**
         * Representa um erro genérico do Cloud Storage.
         * @param exception A exceção original do Firebase.
         */
        data class Generic(val exception: Exception) : Storage
    }

    /**
     * Erros relacionados ao Cloud Firestore.
     */
    sealed interface Firestore : AppError {
        /** O cliente não tem permissão para acessar o documento ou coleção. */
        object PermissionDenied : Firestore
        /** O documento ou coleção solicitada não foi encontrado. */
        object DocumentNotFound : Firestore
        /** Transação abortada pelo usuário. */
        object Aborted : Firestore

        /**
         * Representa um erro genérico do Cloud Firestore.
         * @param exception A exceção original do Firebase.
         */
        data class Generic(val exception: Exception) : Firestore
    }

    /**
     * Erros relacionados ao Firebase Remote Config.
     */
    sealed interface RemoteConfig : AppError {
        data object FetchFailure : RemoteConfig
        data object UpdateUnavailable : RemoteConfig
        data object StreamError : RemoteConfig
        data object MessageInvalid : RemoteConfig
        data object Unknown : RemoteConfig
    }

    /**
     * Erros relacionados ao Firebase In-App Messaging.
     */
    sealed interface InAppMessaging : AppError {
        /** Ocorreu um erro ao tentar exibir uma mensagem. */
        object MessageDisplayError : InAppMessaging
        /** Falha ao baixar uma imagem para uma mensagem no app. */
        object ImageFetchFailed : InAppMessaging
        /**
         * Representa um erro genérico do In-App Messaging.
         * @param exception A exceção original do Firebase.
         */
        data class Generic(val exception: Exception) : InAppMessaging
    }

    /**
     * Erros relacionados ao Firebase Cloud Messaging (FCM).
     */
    sealed interface Messaging : AppError {
        /** Falha ao obter o token de registro do FCM. */
        object TokenFetchFailed : Messaging
        /** Falha ao enviar uma mensagem upstream para o servidor do FCM. */
        object SendMessageFailed : Messaging
        /**
         * Representa um erro genérico do Cloud Messaging.
         * @param exception A exceção original do Firebase.
         */
        data class Generic(val exception: Exception) : Messaging
    }

    /**
     * Erros relacionados ao chamar Cloud Functions for Firebase.
     */
    sealed interface Functions : AppError {
        /** A função que o cliente tentou chamar não foi encontrada. */
        object FunctionNotFound : Functions
        /** A função encontrou um erro interno e não pôde ser executada. */
        object Internal : Functions
        /** A chamada da função excedeu o tempo limite antes de obter uma resposta. */
        object Timeout : Functions
        /**
         * Representa um erro genérico do Cloud Functions.
         * @param exception A exceção original do Firebase.
         */
        data class Generic(val exception: Exception) : Functions
    }

    /**
     * Erros relacionados à API Generative AI (Vertex AI / Gemini).
     */
    sealed interface VertexAI : AppError {
        /** A resposta do modelo foi bloqueada devido às configurações de segurança. */
        object ResponseBlocked : VertexAI
        /** A chave de API fornecida é inválida ou não está autorizada a usar o serviço. */
        object InvalidApiKey : VertexAI
        /** A cota de solicitações para a API ou para o modelo específico foi excedida. */
        object QuotaExceeded : VertexAI
        /** O modelo solicitado não está disponível ou não pôde ser carregado. */
        object ModelUnavailable : VertexAI
        /**
         * Representa um erro genérico da API Vertex AI.
         * @param exception A exceção original.
         */
        data class Generic(val exception: Exception) : VertexAI
    }
}
