package br.wgc.omnibackend.appwrite.utils

import br.wgc.omnibackend.core.utils.AppError
import io.appwrite.exceptions.AppwriteException

/**
 * Mapeia [AppwriteException] e exceções genéricas do Kotlin/Java para o modelo de erros
 * agnóstico [AppError] do OmniBackend.
 *
 * Os códigos HTTP do Appwrite são mapeados da seguinte forma:
 * - `401` → [AppError.Auth.InvalidCredentials]
 * - `404` com contexto de usuário → [AppError.Auth.UserNotFound]
 * - `404` com contexto de documento → [AppError.Firestore.DocumentNotFound]
 * - `404` com contexto de armazenamento → [AppError.Storage.ObjectNotFound]
 * - `409` → [AppError.Auth.EmailAlreadyInUse]
 * - `403` com contexto de auth/db → [AppError.Firestore.PermissionDenied] / [AppError.Auth.Generic]
 * - `507` → [AppError.Storage.QuotaExceeded]
 * - Falha de rede → [AppError.Generic.Network]
 */
internal object AppwriteErrorMapper {

    /**
     * Converte uma [AppwriteException] no [AppError] correspondente.
     *
     * @param e Exceção lançada pelo Appwrite SDK.
     * @param context Contexto de onde o erro ocorreu: "auth", "firestore" ou "storage".
     * @return [AppError] adequado ao domínio da operação.
     */
    fun mapException(e: AppwriteException, context: String = "auth"): AppError {
        val code = e.code ?: 0
        return when {
            isNetworkError(e) -> AppError.Generic.Network
            code == 401 -> AppError.Auth.InvalidCredentials
            code == 409 -> AppError.Auth.EmailAlreadyInUse
            code == 400 && context == "auth" -> AppError.Auth.WeakPassword
            code == 404 && context == "auth" -> AppError.Auth.UserNotFound
            code == 403 && context == "auth" -> AppError.Auth.Generic(e)
            code == 403 && context == "firestore" -> AppError.Firestore.PermissionDenied
            code == 404 && context == "firestore" -> AppError.Firestore.DocumentNotFound
            code == 503 && context == "firestore" -> AppError.Firestore.TransactionFailed
            code == 403 && context == "storage" -> AppError.Storage.PermissionDenied
            code == 404 && context == "storage" -> AppError.Storage.ObjectNotFound
            code == 507 -> AppError.Storage.QuotaExceeded
            context == "auth" -> AppError.Auth.Generic(e)
            context == "firestore" -> AppError.Firestore.Generic(e)
            context == "storage" -> AppError.Storage.Generic(e)
            else -> AppError.Generic.Unknown(e)
        }
    }

    /**
     * Converte qualquer [Throwable] no [AppError] correspondente.
     *
     * @param t Exceção capturada.
     * @param context Contexto da operação.
     * @return [AppError] mapeado.
     */
    fun mapThrowable(t: Throwable, context: String = "auth"): AppError {
        if (t is AppwriteException) return mapException(t, context)
        return if (isNetworkError(t)) AppError.Generic.Network else AppError.Generic.Unknown(t)
    }

    private fun isNetworkError(t: Throwable): Boolean {
        val msg = t.message.orEmpty().lowercase()
        return t is java.net.UnknownHostException ||
            t is java.net.SocketTimeoutException ||
            t is java.net.ConnectException ||
            msg.contains("unable to resolve host") ||
            msg.contains("network") ||
            msg.contains("timeout") ||
            msg.contains("connection")
    }
}
