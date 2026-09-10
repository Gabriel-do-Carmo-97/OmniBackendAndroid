package br.wgc.omnibackend.amplify.utils

import br.wgc.omnibackend.core.utils.AppError
import com.amplifyframework.auth.AuthException
import com.amplifyframework.storage.StorageException

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
            403 -> if (context == "firestore") {
                AppError.Firestore.PermissionDenied
            } else if (context == "storage") {
                AppError.Storage.PermissionDenied
            } else {
                AppError.Auth.InvalidCredentials
            }
            404 -> if (context == "auth") {
                AppError.Auth.UserNotFound
            } else if (context == "storage") {
                AppError.Storage.ObjectNotFound
            } else {
                AppError.Firestore.DocumentNotFound
            }
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
        val className = t::class.java.simpleName
        val ex = (t as? Exception) ?: Exception(t)

        if (t is java.net.UnknownHostException || t is java.net.SocketTimeoutException || msg.contains("network")) {
            return AppError.Generic.Network
        }

        if (t is AuthException) {
            return when {
                className.contains("UserNotFound") || msg.contains("user does not exist") || msg.contains("usernotfound") ->
                    AppError.Auth.UserNotFound
                className.contains("UsernameExists") || msg.contains("username already exists") || msg.contains("userexists") ->
                    AppError.Auth.EmailAlreadyInUse
                className.contains("NotAuthorized") || msg.contains("not authorized") || msg.contains("incorrect username") ->
                    AppError.Auth.InvalidCredentials
                className.contains("InvalidPassword") || msg.contains("password does not conform") ->
                    AppError.Auth.WeakPassword
                className.contains("CodeMismatch") || msg.contains("invalid code") ->
                    AppError.Auth.InvalidCredentials
                className.contains("SessionExpired") || msg.contains("session expired") ->
                    AppError.Auth.InvalidCredentials
                else -> AppError.Auth.Generic(t)
            }
        }

        if (t is StorageException) {
            return when {
                className.contains("NoSuchKey") || msg.contains("not found") || msg.contains("nosuchkey") ->
                    AppError.Storage.ObjectNotFound
                className.contains("AccessDenied") || msg.contains("access denied") || msg.contains("forbidden") ->
                    AppError.Storage.PermissionDenied
                else -> AppError.Storage.Generic(t)
            }
        }

        return when (context) {
            "auth" -> AppError.Auth.Generic(ex)
            "firestore" -> AppError.Firestore.Generic(ex)
            "storage" -> AppError.Storage.Generic(ex)
            else -> AppError.Generic.Unknown(t)
        }
    }
}
