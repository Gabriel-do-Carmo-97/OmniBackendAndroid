package br.wgc.omnibackend.back4app.utils

import br.wgc.omnibackend.core.utils.AppError
import com.parse.ParseException

/**
 * Mapeia exceções do Parse SDK ([ParseException]) e erros genéricos para o modelo [AppError].
 */
internal object Back4AppErrorMapper {

    /**
     * Converte uma [ParseException] para [AppError].
     *
     * @param e Exceção capturada do Parse SDK.
     * @param context Contexto da operação ("auth", "firestore", "storage").
     * @return Instância de [AppError] mapeada.
     */
    fun mapException(e: ParseException, context: String = "auth"): AppError = when (e.code) {
        ParseException.OBJECT_NOT_FOUND -> {
            if (context == "auth") {
                AppError.Auth.UserNotFound
            } else if (context == "storage") {
                AppError.Storage.ObjectNotFound
            } else {
                AppError.Firestore.DocumentNotFound
            }
        }
        ParseException.USERNAME_TAKEN, ParseException.EMAIL_TAKEN -> AppError.Auth.EmailAlreadyInUse
        ParseException.EMAIL_NOT_FOUND -> AppError.Auth.UserNotFound
        ParseException.INVALID_SESSION_TOKEN, ParseException.PASSWORD_MISSING -> AppError.Auth.InvalidCredentials
        ParseException.CONNECTION_FAILED, ParseException.TIMEOUT -> AppError.Generic.Network
        ParseException.OPERATION_FORBIDDEN -> {
            if (context == "firestore") {
                AppError.Firestore.PermissionDenied
            } else if (context == "storage") {
                AppError.Storage.PermissionDenied
            } else {
                AppError.Auth.Generic(e)
            }
        }
        else -> {
            when (context) {
                "auth" -> AppError.Auth.Generic(e)
                "firestore" -> AppError.Firestore.Generic(e)
                "storage" -> AppError.Storage.Generic(e)
                else -> AppError.Generic.Unknown(e)
            }
        }
    }

    /**
     * Converte qualquer [Throwable] para [AppError].
     */
    fun mapThrowable(t: Throwable, context: String = "auth"): AppError {
        if (t is ParseException) return mapException(t, context)
        return AppError.Generic.Unknown(t)
    }
}
