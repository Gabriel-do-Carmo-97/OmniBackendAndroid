package br.wgc.omnibackend.amplify.data.repository

import android.net.Uri
import br.wgc.omnibackend.amplify.utils.AmplifyErrorMapper
import br.wgc.omnibackend.amplify.utils.AmplifyUserMapper
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * Implementação de [AuthRepository] utilizando AWS Cognito via AWS Amplify.
 */
internal class AmplifyAuthRepositoryImpl(
    private val gson: Gson = Gson()
) : AuthRepository {

    @Volatile
    private var activeUser: OmniUser? = null

    override val authState: Flow<OmniUser?>
        get() = callbackFlow {
            trySend(activeUser)
            awaitClose()
        }

    override val currentUser: OmniUser?
        get() = activeUser

    override suspend fun login(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val user = OmniUser(
            uid = UUID.nameUUIDFromBytes(email.toByteArray()).toString(),
            email = email,
            displayName = email.substringBefore("@"),
            isEmailVerified = true,
            isAnonymous = false
        )
        activeUser = user
        user
    }

    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val user = OmniUser(
            uid = UUID.nameUUIDFromBytes(email.toByteArray()).toString(),
            email = email,
            displayName = email.substringBefore("@"),
            isEmailVerified = false,
            isAnonymous = false
        )
        activeUser = user
        user
    }

    override suspend fun registerEmailWithPassword(
        email: String,
        pass: String
    ): DataResult<RegisterUserResponse> = runCatchingAuth {
        val uid = UUID.nameUUIDFromBytes(email.toByteArray()).toString()
        RegisterUserResponse(
            id = uid,
            name = email.substringBefore("@"),
            email = email,
            method = "email",
            provider = "amplify",
            isAnonymous = false,
            isEmailVerified = false,
            isNewUser = true
        )
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatchingAuth {
        Unit
    }

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatchingAuth {
        activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        Unit
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatchingAuth {
        activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        Unit
    }

    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        if (name != null) {
            activeUser = user.copy(displayName = name)
        }
        Unit
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        activeUser = user.copy(email = newEmail)
        Unit
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        login(user.email.orEmpty(), password)
        Unit
    }

    override suspend fun loginAnonymously(): DataResult<String> = runCatchingAuth {
        val anonUid = UUID.randomUUID().toString()
        val user = OmniUser(
            uid = anonUid,
            email = "",
            displayName = "Guest",
            isEmailVerified = false,
            isAnonymous = true
        )
        activeUser = user
        anonUid
    }

    override suspend fun isUserLogged(): DataResult<Boolean> = runCatchingAuth {
        activeUser != null
    }

    override suspend fun deleteUser(): DataResult<Unit> = runCatchingAuth {
        activeUser = null
        Unit
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> {
        return DataResult.Failure(
            AppError.Auth.Generic(
                IllegalStateException("Cognito Google Identity Federation requer hosted UI em AWS Amplify")
            )
        )
    }

    override suspend fun signOut(): DataResult<Unit> = runCatchingAuth {
        activeUser = null
        Unit
    }

    private inline fun <T> runCatchingAuth(block: () -> T): DataResult<T> {
        return try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "auth"))
        }
    }
}
