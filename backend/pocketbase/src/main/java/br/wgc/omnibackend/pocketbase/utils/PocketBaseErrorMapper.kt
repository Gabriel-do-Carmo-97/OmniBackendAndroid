package br.wgc.omnibackend.pocketbase.utils

import br.wgc.omnibackend.core.utils.AppError

/**
 * Mapeia erros de resposta HTTP e exceções do PocketBase para [AppError].
 */
internal object PocketBaseErrorMapper {

    /**
     * Mapeia um código de status HTTP do PocketBase para [AppError].
     *
     * @param statusCode Código de resposta HTTP.
     * @param context Contexto ("auth", "firestore", "storage").
     * @param cause Exceção original opcional.
     * @return [AppError] mapeado.
     */
    fun mapStatusCode(statusCode: Int, context: String = "auth", cause: Throwable? = null): AppError {
        val ex = (cause as? Exception) ?: Exception(cause?.message ?: "PocketBase error: $statusCode")
        return when (statusCode) {
            400 -> if (context == "auth") AppError.Auth.WeakPassword else AppError.Generic.Unknown(ex)
            401 -> AppError.Auth.InvalidCredentials
            403 -> if (context == "firestore") AppError.Firestore.PermissionDenied else if (context == "storage") AppError.Storage.PermissionDenied else AppError.Auth.InvalidCredentials
            404 -> if (context == "auth") AppError.Auth.UserNotFound else if (context == "storage") AppError.Storage.ObjectNotFound else AppError.Firestore.DocumentNotFound
            409 -> AppError.Auth.EmailAlreadyInUse
            413 -> AppError.Storage.QuotaExceeded
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
