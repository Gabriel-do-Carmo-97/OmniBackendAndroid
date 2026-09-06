package br.wgc.omnibackend.rest.utils

import br.wgc.omnibackend.core.utils.AppError

/**
 * Mapeia erros de status HTTP e payloads RFC 7807 (Problem Details) da API REST para [AppError].
 */
internal object RestErrorMapper {

    /**
     * Mapeia um código de status HTTP da API REST para [AppError].
     *
     * @param statusCode Status HTTP da resposta.
     * @param context Contexto ("auth", "firestore", "storage").
     * @param cause Exceção original opcional.
     * @return [AppError] mapeado.
     */
    fun mapStatusCode(statusCode: Int, context: String = "auth", cause: Throwable? = null): AppError {
        val ex = (cause as? Exception) ?: Exception(cause?.message ?: "REST API HTTP error $statusCode")
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
