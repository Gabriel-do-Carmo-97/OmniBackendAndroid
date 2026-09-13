package br.wgc.omnibackend.back4app.data.repository

import android.net.Uri
import br.wgc.omnibackend.back4app.utils.Back4AppErrorMapper
import br.wgc.omnibackend.back4app.utils.Back4AppUserMapper
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.parse.ParseException
import com.parse.ParseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * Implementação concreta de [AuthRepository] para Back4App utilizando [ParseUser].
 */
internal class Back4AppAuthRepositoryImpl : AuthRepository {

    override val authState: Flow<OmniUser?>
        get() = callbackFlow {
            val user = ParseUser.getCurrentUser()
            trySend(user?.let { Back4AppUserMapper.toOmniUser(it) })
            awaitClose()
        }

    override val currentUser: OmniUser?
        get() = ParseUser.getCurrentUser()?.let { Back4AppUserMapper.toOmniUser(it) }

    override suspend fun login(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val user = ParseUser.logIn(email, pass)
        Back4AppUserMapper.toOmniUser(user)
    }

    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val user = ParseUser().apply {
            username = email
            setEmail(email)
            setPassword(pass)
            signUp()
        }
        Back4AppUserMapper.toOmniUser(user)
    }

    override suspend fun registerEmailWithPassword(email: String, pass: String): DataResult<RegisterUserResponse> = runCatchingAuth {
        val user = ParseUser().apply {
            username = email
            setEmail(email)
            setPassword(pass)
            signUp()
        }
        RegisterUserResponse(
            id = user.objectId,
            name = user.username,
            email = user.email,
            method = "email",
            provider = "back4app",
            isAnonymous = false,
            isEmailVerified = user.getBoolean("emailVerified"),
            isNewUser = true,
        )
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatchingAuth {
        ParseUser.requestPasswordReset(email)
        Unit
    }

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatchingAuth {
        val user = ParseUser.getCurrentUser() ?: throw ParseException(ParseException.OBJECT_NOT_FOUND, "User not logged in")
        user.setPassword(newPassword)
        user.save()
        Unit
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatchingAuth {
        val user = ParseUser.getCurrentUser() ?: throw ParseException(ParseException.OBJECT_NOT_FOUND, "User not logged in")
        user.save()
        Unit
    }

    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatchingAuth {
        val user = ParseUser.getCurrentUser() ?: throw ParseException(ParseException.OBJECT_NOT_FOUND, "User not logged in")
        if (name != null) user.put("name", name)
        user.save()
        Unit
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatchingAuth {
        val user = ParseUser.getCurrentUser() ?: throw ParseException(ParseException.OBJECT_NOT_FOUND, "User not logged in")
        user.setEmail(newEmail)
        user.save()
        Unit
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatchingAuth {
        val user = ParseUser.getCurrentUser() ?: throw ParseException(ParseException.OBJECT_NOT_FOUND, "User not logged in")
        ParseUser.logIn(user.username, password)
        Unit
    }

    override suspend fun loginAnonymously(): DataResult<String> = runCatchingAuth {
        val anonName = "anon_${UUID.randomUUID()}"
        val user = ParseUser().apply {
            username = anonName
            setPassword(UUID.randomUUID().toString())
            signUp()
        }
        user.objectId
    }

    override suspend fun isUserLogged(): DataResult<Boolean> = runCatchingAuth {
        ParseUser.getCurrentUser() != null
    }

    override suspend fun deleteUser(): DataResult<Unit> = runCatchingAuth {
        val user = ParseUser.getCurrentUser() ?: throw ParseException(ParseException.OBJECT_NOT_FOUND, "User not logged in")
        user.delete()
        ParseUser.logOut()
        Unit
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> = DataResult.Failure(
        AppError.Auth.Generic(
            IllegalStateException("OAuth2 para Google em Back4App requer integração via Activity e ParseFacebookUtils/ParseGoogleUtils"),
        ),
    )

    override suspend fun signOut(): DataResult<Unit> = runCatchingAuth {
        ParseUser.logOut()
        Unit
    }

    private inline fun <T> runCatchingAuth(block: () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: ParseException) {
        DataResult.Failure(Back4AppErrorMapper.mapException(e, "auth"))
    } catch (e: Exception) {
        DataResult.Failure(Back4AppErrorMapper.mapThrowable(e, "auth"))
    }
}
