package br.wgc.omnibackend.amplify.data.repository

import android.net.Uri
import br.wgc.omnibackend.amplify.utils.AmplifyErrorMapper
import br.wgc.omnibackend.amplify.utils.AmplifyUserMapper
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.kotlin.core.Amplify
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * Implementação corporativa de [AuthRepository] utilizando o SDK oficial do AWS Cognito via AWS Amplify.
 */
internal class AmplifyAuthRepositoryImpl : AuthRepository {

    @Volatile
    private var activeUser: OmniUser? = null

    override val authState: Flow<OmniUser?>
        get() = callbackFlow {
            val user = try {
                val current = Amplify.Auth.getCurrentUser()
                val attributes = try {
                    Amplify.Auth.fetchUserAttributes()
                } catch (_: Exception) {
                    emptyList()
                }
                val mapped = AmplifyUserMapper.toOmniUser(current, attributes)
                activeUser = mapped
                mapped
            } catch (_: Exception) {
                activeUser = null
                null
            }
            trySend(user)
            awaitClose()
        }

    override val currentUser: OmniUser?
        get() = activeUser

    override suspend fun login(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val result = Amplify.Auth.signIn(email, pass)
        val user = if (result.isSignedIn) {
            val current = Amplify.Auth.getCurrentUser()
            val attributes = try {
                Amplify.Auth.fetchUserAttributes()
            } catch (_: Exception) {
                emptyList()
            }
            AmplifyUserMapper.toOmniUser(current, attributes)
        } else {
            OmniUser(
                uid = UUID.nameUUIDFromBytes(email.toByteArray()).toString(),
                email = email,
                displayName = email.substringBefore("@"),
                isEmailVerified = false,
                isAnonymous = false,
            )
        }
        activeUser = user
        user
    }

    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), email)
            .build()
        val result = Amplify.Auth.signUp(email, pass, options)
        val uid = result.userId ?: UUID.nameUUIDFromBytes(email.toByteArray()).toString()
        val user = OmniUser(
            uid = uid,
            email = email,
            displayName = email.substringBefore("@"),
            isEmailVerified = result.isSignUpComplete,
            isAnonymous = false,
        )
        activeUser = user
        user
    }

    override suspend fun registerEmailWithPassword(email: String, pass: String): DataResult<RegisterUserResponse> = runCatchingAuth {
        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), email)
            .build()
        val result = Amplify.Auth.signUp(email, pass, options)
        val uid = result.userId ?: UUID.nameUUIDFromBytes(email.toByteArray()).toString()
        RegisterUserResponse(
            id = uid,
            name = email.substringBefore("@"),
            email = email,
            method = "email",
            provider = "amplify",
            isAnonymous = false,
            isEmailVerified = result.isSignUpComplete,
            isNewUser = true,
        )
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatchingAuth {
        Amplify.Auth.resetPassword(email)
    }

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatchingAuth {
        Amplify.Auth.updatePassword("", newPassword)
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatchingAuth {
        val email = activeUser?.email.orEmpty()
        Amplify.Auth.resendSignUpCode(email)
    }

    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatchingAuth {
        if (name != null) {
            Amplify.Auth.updateUserAttribute(AuthUserAttribute(AuthUserAttributeKey.name(), name))
            activeUser = activeUser?.copy(displayName = name)
        }
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatchingAuth {
        Amplify.Auth.updateUserAttribute(AuthUserAttribute(AuthUserAttributeKey.email(), newEmail))
        activeUser = activeUser?.copy(email = newEmail)
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatchingAuth {
        val email = activeUser?.email.orEmpty()
        Amplify.Auth.signIn(email, password)
    }

    override suspend fun loginAnonymously(): DataResult<String> = runCatchingAuth {
        val session = Amplify.Auth.fetchAuthSession()
        val identityId = (session as? AWSCognitoAuthSession)?.identityIdResult?.value
            ?: UUID.randomUUID().toString()
        val anonUser = OmniUser(
            uid = identityId,
            email = "",
            displayName = "Guest",
            isEmailVerified = false,
            isAnonymous = true,
        )
        activeUser = anonUser
        identityId
    }

    override suspend fun isUserLogged(): DataResult<Boolean> = runCatchingAuth {
        val session = Amplify.Auth.fetchAuthSession()
        session.isSignedIn
    }

    override suspend fun deleteUser(): DataResult<Unit> = runCatchingAuth {
        Amplify.Auth.deleteUser()
        activeUser = null
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> = DataResult.Failure(
        AppError.Auth.Generic(
            IllegalStateException("AWS Cognito Google Identity Federation requer Hosted UI Web / Amplify.Auth.signInWithSocialWebUI"),
        ),
    )

    override suspend fun signOut(): DataResult<Unit> = runCatchingAuth {
        val _res = Amplify.Auth.signOut()
        activeUser = null
    }

    private suspend inline fun <T> runCatchingAuth(crossinline block: suspend () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Failure(AmplifyErrorMapper.mapThrowable(e, "auth"))
    }
}
