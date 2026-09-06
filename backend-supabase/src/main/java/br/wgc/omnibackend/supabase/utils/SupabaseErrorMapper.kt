package br.wgc.omnibackend.supabase.utils

import br.wgc.omnibackend.core.utils.AppError
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Utilitário responsável pelo mapeamento de exceções do Supabase, Ktor e rede
 * para a hierarquia tipada e agnóstica de [AppError].
 */
internal object SupabaseErrorMapper {

    /**
     * Mapeia falhas do fluxo de autenticação para [AppError.Auth] ou [AppError.Generic].
     *
     * @param throwable Exceção capturada na operação de autenticação.
     * @return [AppError] tipado.
     */
    fun mapAuthError(throwable: Throwable): AppError = when {
        isNetworkError(throwable) -> AppError.Generic.Network
        throwable is RestException -> {
            val message = throwable.message?.lowercase() ?: ""
            when {
                throwable.statusCode == 400 && (message.contains("user already registered") || message.contains("email already in use") || message.contains("already exists")) ->
                    AppError.Auth.EmailAlreadyInUse
                throwable.statusCode == 400 && (message.contains("password") || message.contains("weak")) ->
                    AppError.Auth.WeakPassword
                throwable.statusCode == 400 && (message.contains("invalid login credentials") || message.contains("invalid_grant")) ->
                    AppError.Auth.InvalidCredentials
                throwable.statusCode == 404 || message.contains("user not found") ->
                    AppError.Auth.UserNotFound
                throwable.statusCode == 401 ->
                    AppError.Auth.InvalidCredentials
                else -> AppError.Auth.Generic(Exception(throwable.message, throwable))
            }
        }
        else -> AppError.Auth.Generic(Exception(throwable.message, throwable))
    }

    /**
     * Mapeia falhas de banco de dados (PostgREST / Realtime) para [AppError.Firestore] ou [AppError.Generic].
     *
     * @param throwable Exceção capturada na operação de banco de dados.
     * @return [AppError] tipado.
     */
    fun mapDatabaseError(throwable: Throwable): AppError = when {
        isNetworkError(throwable) -> AppError.Generic.Network
        throwable is RestException -> {
            when (throwable.statusCode) {
                401, 403 -> AppError.Firestore.PermissionDenied
                404 -> AppError.Firestore.DocumentNotFound
                409 -> AppError.Firestore.TransactionFailed
                else -> AppError.Firestore.Generic(Exception(throwable.message, throwable))
            }
        }
        else -> AppError.Firestore.Generic(Exception(throwable.message, throwable))
    }

    /**
     * Mapeia falhas de armazenamento de arquivos (Storage) para [AppError.Storage] ou [AppError.Generic].
     *
     * @param throwable Exceção capturada na operação de storage.
     * @return [AppError] tipado.
     */
    fun mapStorageError(throwable: Throwable): AppError = when {
        isNetworkError(throwable) -> AppError.Generic.Network
        throwable is RestException -> {
            when (throwable.statusCode) {
                401, 403 -> AppError.Storage.PermissionDenied
                404 -> AppError.Storage.ObjectNotFound
                413 -> AppError.Storage.QuotaExceeded
                else -> AppError.Storage.Generic(Exception(throwable.message, throwable))
            }
        }
        else -> AppError.Storage.Generic(Exception(throwable.message, throwable))
    }

    private fun isNetworkError(throwable: Throwable): Boolean =
        throwable is IOException ||
        throwable is UnknownHostException ||
        throwable is ConnectException ||
        throwable is HttpRequestTimeoutException
}
