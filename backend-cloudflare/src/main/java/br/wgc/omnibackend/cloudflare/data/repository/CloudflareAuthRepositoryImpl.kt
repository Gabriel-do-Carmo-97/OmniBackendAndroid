package br.wgc.omnibackend.cloudflare.data.repository

import android.net.Uri
import br.wgc.omnibackend.cloudflare.utils.CloudflareErrorMapper
import br.wgc.omnibackend.cloudflare.utils.CloudflareUserMapper
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Implementação de [AuthRepository] delegando autenticação para Cloudflare Worker endpoints (`/auth/*`).
 */
internal class CloudflareAuthRepositoryImpl(
    private val workerBaseUrl: String,
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
        val url = "$workerBaseUrl/auth/login"
        val response = postJson(url, mapOf("email" to email, "password" to pass))
        val user = CloudflareUserMapper.toOmniUser(response)
        activeUser = user
        user
    }

    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val url = "$workerBaseUrl/auth/register"
        val response = postJson(url, mapOf("email" to email, "password" to pass))
        val user = CloudflareUserMapper.toOmniUser(response)
        activeUser = user
        user
    }

    override suspend fun registerEmailWithPassword(
        email: String,
        pass: String
    ): DataResult<RegisterUserResponse> = runCatchingAuth {
        val url = "$workerBaseUrl/auth/register"
        val response = postJson(url, mapOf("email" to email, "password" to pass))
        val uid = response["uid"]?.toString() ?: response["id"]?.toString().orEmpty()
        val createdEmail = response["email"]?.toString().orEmpty()

        RegisterUserResponse(
            id = uid,
            name = response["displayName"]?.toString(),
            email = createdEmail,
            method = "email",
            provider = "cloudflare",
            isAnonymous = false,
            isEmailVerified = response["isEmailVerified"] as? Boolean ?: false,
            isNewUser = true
        )
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatchingAuth {
        postJson("$workerBaseUrl/auth/reset-password", mapOf("email" to email))
        Unit
    }

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        postJson("$workerBaseUrl/auth/update-password", mapOf("uid" to user.uid, "newPassword" to newPassword))
        Unit
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        postJson("$workerBaseUrl/auth/verify-email", mapOf("uid" to user.uid))
        Unit
    }

    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        if (name != null) {
            postJson("$workerBaseUrl/auth/update-profile", mapOf("uid" to user.uid, "displayName" to name))
            activeUser = user.copy(displayName = name)
        }
        Unit
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        postJson("$workerBaseUrl/auth/update-email", mapOf("uid" to user.uid, "newEmail" to newEmail))
        Unit
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        login(user.email.orEmpty(), password)
        Unit
    }

    override suspend fun loginAnonymously(): DataResult<String> = runCatchingAuth {
        val anonId = UUID.randomUUID().toString()
        val response = postJson("$workerBaseUrl/auth/anonymous", mapOf("anonId" to anonId))
        val uid = response["uid"]?.toString() ?: anonId
        uid
    }

    override suspend fun isUserLogged(): DataResult<Boolean> = runCatchingAuth {
        activeUser != null
    }

    override suspend fun deleteUser(): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        postJson("$workerBaseUrl/auth/delete", mapOf("uid" to user.uid))
        activeUser = null
        Unit
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> = runCatchingAuth {
        val response = postJson("$workerBaseUrl/auth/google", mapOf("idToken" to idToken))
        val user = CloudflareUserMapper.toOmniUser(response)
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
            throw IllegalStateException("Cloudflare Worker HTTP $code: ${conn.responseMessage}")
        }
    }

    private inline fun <T> runCatchingAuth(block: () -> T): DataResult<T> {
        return try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Failure(CloudflareErrorMapper.mapThrowable(e, "auth"))
        }
    }
}
