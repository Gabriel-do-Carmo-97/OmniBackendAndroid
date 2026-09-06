package br.wgc.omnibackend.rest.data.repository

import android.net.Uri
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.rest.utils.RestErrorMapper
import br.wgc.omnibackend.rest.utils.RestUserMapper
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Implementação de [AuthRepository] interagindo com endpoints REST de autenticação.
 */
internal class RestAuthRepositoryImpl(
    private val baseUrl: String,
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
        val url = "$baseUrl/auth/login"
        val response = postJson(url, mapOf("email" to email, "password" to pass))
        val user = RestUserMapper.toOmniUser(response)
        activeUser = user
        user
    }

    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val url = "$baseUrl/auth/register"
        val response = postJson(url, mapOf("email" to email, "password" to pass))
        val user = RestUserMapper.toOmniUser(response)
        activeUser = user
        user
    }

    override suspend fun registerEmailWithPassword(
        email: String,
        pass: String
    ): DataResult<RegisterUserResponse> = runCatchingAuth {
        val url = "$baseUrl/auth/register"
        val response = postJson(url, mapOf("email" to email, "password" to pass))
        val uid = response["id"]?.toString() ?: response["uid"]?.toString().orEmpty()

        RegisterUserResponse(
            id = uid,
            name = response["name"]?.toString(),
            email = response["email"]?.toString().orEmpty(),
            method = "email",
            provider = "rest",
            isAnonymous = false,
            isEmailVerified = response["isEmailVerified"] as? Boolean ?: false,
            isNewUser = true
        )
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatchingAuth {
        postJson("$baseUrl/auth/reset-password", mapOf("email" to email))
        Unit
    }

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        postJson("$baseUrl/auth/update-password", mapOf("uid" to user.uid, "password" to newPassword))
        Unit
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        postJson("$baseUrl/auth/send-verification", mapOf("uid" to user.uid))
        Unit
    }

    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        if (name != null) {
            postJson("$baseUrl/auth/update-profile", mapOf("uid" to user.uid, "name" to name))
            activeUser = user.copy(displayName = name)
        }
        Unit
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        postJson("$baseUrl/auth/update-email", mapOf("uid" to user.uid, "email" to newEmail))
        Unit
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        login(user.email.orEmpty(), password)
        Unit
    }

    override suspend fun loginAnonymously(): DataResult<String> = runCatchingAuth {
        val anonId = UUID.randomUUID().toString()
        val response = postJson("$baseUrl/auth/anonymous", mapOf("anonId" to anonId))
        val uid = response["id"]?.toString() ?: response["uid"]?.toString() ?: anonId
        uid
    }

    override suspend fun isUserLogged(): DataResult<Boolean> = runCatchingAuth {
        activeUser != null
    }

    override suspend fun deleteUser(): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        postJson("$baseUrl/auth/delete", mapOf("uid" to user.uid))
        activeUser = null
        Unit
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> = runCatchingAuth {
        val response = postJson("$baseUrl/auth/google", mapOf("idToken" to idToken))
        val user = RestUserMapper.toOmniUser(response)
        activeUser = user
        user
    }

    override suspend fun signOut(): DataResult<Unit> = runCatchingAuth {
        activeUser = null
        Unit
    }

    // --- HTTP Helpers ---

    @Suppress("UNCHECKED_CAST")
    private fun postJson(urlString: String, bodyMap: Map<String, Any?>): Map<String, Any?> {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }

        val json = gson.toJson(bodyMap)
        conn.outputStream.use { it.write(json.toByteArray()) }

        val code = conn.responseCode
        if (code in 200..299) {
            val responseText = conn.inputStream.use { it.bufferedReader().readText() }
            return if (responseText.isNotBlank()) {
                gson.fromJson(responseText, Map::class.java) as Map<String, Any?>
            } else emptyMap()
        } else {
            throw IllegalStateException("REST API HTTP $code: ${conn.responseMessage}")
        }
    }

    private inline fun <T> runCatchingAuth(block: () -> T): DataResult<T> {
        return try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Failure(RestErrorMapper.mapThrowable(e, "auth"))
        }
    }
}
