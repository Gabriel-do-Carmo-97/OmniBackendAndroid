package br.wgc.omnibackend.core.utils

/**
 * Define a hierarquia exaustiva de erros agnóstica do ecossistema OmniBackend.
 *
 * A estrutura baseada em `sealed interface` categoriza falhas por domínio funcional,
 * permitindo `when` exaustivos sem dependência de classes de exceção proprietárias de SDKs.
 */
sealed interface AppError {

    /**
     * Erros genéricos de conectividade, rede e exceções não mapeadas.
     */
    sealed interface Generic : AppError {
        /** Falha de conexão de rede ou ausência de conectividade com a internet. */
        object Network : Generic

        /** Exceção não mapeada que encapsula o [throwable] original. */
        data class Unknown(val throwable: Throwable) : Generic

        /** Falha genérica contendo mensagem descritiva [msg]. */
        data class GenericException(val msg: String) : Generic
    }

    /**
     * Falhas relativas ao fluxo de autenticação e gerenciamento de identidade.
     */
    sealed interface Auth : AppError {
        /** O e-mail fornecido já está cadastrado em outra conta no provedor. */
        object EmailAlreadyInUse : Auth

        /** A senha fornecida não cumpre os critérios mínimos de segurança. */
        object WeakPassword : Auth

        /** As credenciais de login informadas (usuário ou senha) são incorretas. */
        object InvalidCredentials : Auth

        /** Nenhum registro de usuário correspondente foi localizado. */
        object UserNotFound : Auth

        /** O código de validação de ação/link enviado por e-mail expirou ou é inválido. */
        object InvalidActionCode : Auth

        /** Falha no disparo do e-mail de verificação ou redefinição de senha. */
        object EmailSendFailed : Auth

        /** A operação requer reautenticação recente do usuário por questões de segurança. */
        object RequiresRecentLogin : Auth

        /** O desafio reCAPTCHA ou token de validação de bot não pôde ser completado. */
        object RecaptchaActivityMissing : Auth

        /** Falha na comunicação com o fluxo web de autenticação OAuth. */
        object WebOperationFailed : Auth

        /** Exceção genérica de autenticação encapsulando [exception]. */
        data class Generic(val exception: Exception) : Auth

        /** A conta requer autenticação de múltiplos fatores (MFA). */
        data class MultiFactorRequired(val cause: Exception? = null) : Auth
    }

    /**
     * Falhas em operações de bancos de dados em tempo real (Realtime Database).
     */
    sealed interface RealtimeDatabase : AppError {
        /** Permissão negada pelas regras de segurança do banco. */
        object PermissionDenied : RealtimeDatabase

        /** Os dados locais estão desatualizados em relação ao servidor. */
        object DataStale : RealtimeDatabase

        /** O cliente encontra-se desconectado do cluster de tempo real. */
        object Disconnected : RealtimeDatabase

        /** O token de autenticação de sessão expirou durante a sincronização. */
        object ExpiredToken : RealtimeDatabase

        /** O token de acesso informado possui assinatura inválida. */
        object InvalidToken : RealtimeDatabase

        /** Número máximo de tentativas de reconexão ou transação excedido. */
        object MaxRetries : RealtimeDatabase

        /** A operação de escrita local foi sobrescrita por uma gravação remota concorrente. */
        object OverriddenBySet : RealtimeDatabase

        /** O serviço de banco em tempo real está temporariamente indisponível. */
        object Unavailable : RealtimeDatabase

        /** A operação de escrita foi cancelada antes de sua confirmação. */
        object WriteCanceled : RealtimeDatabase

        /** Falha operacional genérica no banco em tempo real. */
        object OperationFailed : RealtimeDatabase

        /** Erro inesperado no banco em tempo real encapsulando [exception]. */
        data class Generic(val exception: Exception) : RealtimeDatabase
    }

    /**
     * Falhas em operações de armazenamento de arquivos (Storage / Buckets).
     */
    sealed interface Storage : AppError {
        /** O arquivo ou objeto solicitado não foi encontrado no caminho especificado. */
        object ObjectNotFound : Storage

        /** A cota de armazenamento ou de transferência do bucket foi excedida. */
        object QuotaExceeded : Storage

        /** Acesso negado para leitura ou escrita no caminho do bucket. */
        object PermissionDenied : Storage

        /** O bucket de armazenamento configurado não existe. */
        object BucketNotFound : Storage

        /** O projeto em nuvem associado ao bucket não foi localizado. */
        object ProjectNotFound : Storage

        /** O upload do arquivo foi explicitamente cancelado pelo usuário ou sistema. */
        object UploadCancelled : Storage

        /** Falha no download do conteúdo do arquivo. */
        object DownloadFailed : Storage

        /** Erro genérico de armazenamento encapsulando [exception]. */
        data class Generic(val exception: Exception) : Storage
    }

    /**
     * Falhas em operações com bancos NoSQL orientados a documentos (Firestore / PostgREST / etc.).
     */
    sealed interface Firestore : AppError {
        /** Acesso negado pelas regras de segurança da coleção ou documento. */
        object PermissionDenied : Firestore

        /** O documento pesquisado não existe na coleção. */
        object DocumentNotFound : Firestore

        /** A transação ou operação foi abortada pelo servidor. */
        object Aborted : Firestore

        /** A transação no documento falhou por conflito ou concorrência. */
        object TransactionFailed : Firestore

        /** Erro genérico no banco de documentos encapsulando [exception]. */
        data class Generic(val exception: Exception) : Firestore
    }

    /**
     * Falhas no serviço de configuração remota (Remote Config).
     */
    sealed interface RemoteConfig : AppError {
        /** Falha no download das variáveis remotas a partir do servidor. */
        data object FetchFailure : RemoteConfig

        /** Atualização remota temporariamente indisponível. */
        data object UpdateUnavailable : RemoteConfig

        /** Erro no fluxo de escuta contínua de alterações remotas. */
        data object StreamError : RemoteConfig

        /** Mensagem de configuração remota inválida ou corrompida. */
        data object MessageInvalid : RemoteConfig

        /** Erro desconhecido ao processar configurações remotas. */
        data object Unknown : RemoteConfig
    }

    /**
     * Falhas em mensagens contextuais no aplicativo (In-App Messaging).
     */
    sealed interface InAppMessaging : AppError {
        /** Erro ao renderizar a mensagem na interface gráfica. */
        object MessageDisplayError : InAppMessaging

        /** Falha no carregamento da imagem associada à mensagem. */
        object ImageFetchFailed : InAppMessaging

        /** Falha genérica de In-App Messaging encapsulando [exception]. */
        data class Generic(val exception: Exception) : InAppMessaging
    }

    /**
     * Falhas em serviços de mensageria e notificações push (Cloud Messaging).
     */
    sealed interface Messaging : AppError {
        /** Falha ao obter o token de registro de dispositivo para notificações. */
        object TokenFetchFailed : Messaging

        /** Erro no envio de mensagem ou notificação upstream. */
        object SendMessageFailed : Messaging

        /** Falha genérica de mensageria encapsulando [exception]. */
        data class Generic(val exception: Exception) : Messaging
    }

    /**
     * Falhas na invocação de funções serverless em nuvem (Cloud Functions).
     */
    sealed interface Functions : AppError {
        /** A função serverless solicitada não foi encontrada no endpoint configurado. */
        object FunctionNotFound : Functions

        /** Erro interno durante a execução do código no servidor. */
        object Internal : Functions

        /** O tempo limite para resposta da função serverless expirou. */
        object Timeout : Functions

        /** Falha genérica de execução serverless encapsulando [exception]. */
        data class Generic(val exception: Exception) : Functions
    }

    /**
     * Falhas na integração com APIs de IA generativa (Gemini / Vertex AI).
     */
    sealed interface VertexAI : AppError {
        /** A resposta gerada pelo modelo foi bloqueada pelas políticas de segurança/filtro de conteúdo. */
        object ResponseBlocked : VertexAI

        /** Chave de API ou credencial de acesso ao modelo de IA inválida. */
        object InvalidApiKey : VertexAI

        /** Cota de tokens ou requisições por minuto da IA atingida. */
        object QuotaExceeded : VertexAI

        /** O modelo generativo solicitado encontra-se temporariamente indisponível. */
        object ModelUnavailable : VertexAI

        /** Falha genérica na API de IA encapsulando [exception]. */
        data class Generic(val exception: Exception) : VertexAI
    }
}

/**
 * Retorna uma mensagem legível para o usuário correspondente à falha [AppError].
 */
val AppError.message: String
    get() = when (this) {
        is AppError.Generic.Network -> "Sem conexão com a internet. Verifique sua rede e tente novamente."
        is AppError.Generic.GenericException -> msg
        is AppError.Generic.Unknown -> throwable.message ?: "Ocorreu um erro inesperado."
        is AppError.Auth.EmailAlreadyInUse -> "Este endereço de e-mail já está cadastrado."
        is AppError.Auth.WeakPassword -> "Sua senha é muito fraca. Por favor, use uma senha mais forte."
        is AppError.Auth.InvalidCredentials -> "E-mail ou senha inválidos."
        is AppError.Auth.UserNotFound -> "Nenhum usuário encontrado com este e-mail."
        is AppError.Auth.RequiresRecentLogin -> "Esta operação é sensível e requer uma nova autenticação."
        is AppError.Auth.InvalidActionCode -> "O link de verificação é inválido ou já expirou."
        is AppError.Auth.EmailSendFailed -> "Não foi possível enviar o e-mail de verificação."
        is AppError.Auth.RecaptchaActivityMissing -> "Não foi possível verificar o aplicativo."
        is AppError.Auth.WebOperationFailed -> "A autenticação através do serviço web falhou."
        is AppError.Auth.Generic -> exception.message ?: "Ocorreu um erro de autenticação."
        is AppError.Auth.MultiFactorRequired -> "Esta conta requer verificação em duas etapas."
        is AppError.Firestore.DocumentNotFound -> "O documento solicitado não foi encontrado."
        is AppError.Firestore.PermissionDenied -> "Permissão negada para acessar este documento."
        is AppError.Firestore.TransactionFailed -> "A transação falhou devido a um conflito de concorrência."
        is AppError.Firestore.Aborted -> "A operação foi cancelada."
        is AppError.Firestore.Generic -> exception.message ?: "Erro no banco de dados."
        is AppError.Storage.ObjectNotFound -> "O arquivo não foi encontrado no servidor."
        is AppError.Storage.PermissionDenied -> "Permissão negada para acessar este arquivo."
        is AppError.Storage.QuotaExceeded -> "Limite de armazenamento atingido."
        is AppError.Storage.Generic -> exception.message ?: "Erro no armazenamento de arquivos."
        else -> javaClass.simpleName
    }

