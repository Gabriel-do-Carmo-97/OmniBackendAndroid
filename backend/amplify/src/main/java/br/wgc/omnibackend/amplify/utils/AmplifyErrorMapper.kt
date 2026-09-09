package br.wgc.omnibackend.amplify.utils

import br.wgc.omnibackend.core.utils.AppError

/**
 * Mapeia erros e exceções do AWS Amplify/Cognito/S3 para [AppError].
 */
internal object AmplifyErrorMapper {

    /**
     * Mapeia um código de status de erro do AWS Amplify para [AppError].
     */
    fun mapStatusCode(statusCode: Int, context: String = "auth", cause: Throwable? = null): AppError {
        val ex = (cause as? Exception) ?: Exception(cause?.message ?: "AWS Amplify error $statusCode")
        return when (statusCode) {
            401 -> AppError.Auth.InvalidCredentials
            403 -> if (context == "firestore") AppError.Firestore.PermissionDenied else if (context == "storage") AppError.Storage.PermissionDenied else AppError.Auth.InvalidCredentials
            404 -> if (context == "auth") AppError.Auth.UserNotFound else if (context == "storage") AppError.Storage.ObjectNotFound else AppError.Firestore.DocumentNotFound
            409 -> AppError.Auth.EmailAlreadyInUse
            else -> when (context) {
                "auth" -> AppError.Auth.Generic(ex)
                "firestore" -> AppError.Firestore.Generic(ex)
                "storage" -> AppError.Storage.Generic(ex)
                else -> AppError.Generic.Unknown(ex)
            }
        }
    }

    /**
     * Mapeia qualquer [Throwable] para [AppError].
     */
    fun mapThrowable(t: Throwable, context: String = "auth"): AppError {
        val msg = t.message.orEmpty().lowercase()
        val ex = (t as? Exception) ?: Exception(t)
        return if (t is java.net.UnknownHostException || t is java.net.SocketTimeoutException || msg.contains("network")) {
            AppError.Generic.Network
        } else {
            when (context) {
                "auth" -> AppError.Auth.Generic(ex)
                "firestore" -> AppError.Firestore.Generic(ex)
                "storage" -> AppError.Storage.Generic(ex)
                else -> AppError.Generic.Unknown(t)
            }
        }
    }
}
