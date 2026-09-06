package br.wgc.omnibackend.pocketbase.data.repository

import android.net.Uri
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.pocketbase.utils.PocketBaseErrorMapper
import br.wgc.omnibackend.pocketbase.utils.PocketBaseUserMapper
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Implementação de [AuthRepository] utilizando a API do PocketBase (`/api/collections/users/*`).
 */
internal class PocketBaseAuthRepositoryImpl(
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
        val url = "$baseUrl/api/collections/users/auth-with-password"
        val body = mapOf("identity" to email, "password" to pass)
        val response = postJson(url, body)
        @Suppress("UNCHECKED_CAST")
        val record = response["record"] as? Map<String, Any?>
            ?: throw IllegalStateException("Invalid record response from PocketBase")
        val user = PocketBaseUserMapper.toOmniUser(record, baseUrl)
        activeUser = user
        user
    }

    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val url = "$baseUrl/api/collections/users/records"
        val body = mapOf(
            "email" to email,
            "password" to pass,
            "passwordConfirm" to pass,
            "emailVisibility" to true
        )
        val record = postJson(url, body)
        val user = PocketBaseUserMapper.toOmniUser(record, baseUrl)
        activeUser = user
        user
    }

    override suspend fun registerEmailWithPassword(
        email: String,
        pass: String
    ): DataResult<RegisterUserResponse> = runCatchingAuth {
        val url = "$baseUrl/api/collections/users/records"
        val body = mapOf(
            "email" to email,
            "password" to pass,
            "passwordConfirm" to pass,
            "emailVisibility" to true
        )
        val record = postJson(url, body)
        val id = record["id"]?.toString().orEmpty()
        val createdEmail = record["email"]?.toString().orEmpty()
        val name = record["name"]?.toString()
        val verified = record["verified"] as? Boolean ?: false

        RegisterUserResponse(
            id = id,
            name = name,
            email = createdEmail,
            method = "email",
            provider = "pocketbase",
            isAnonymous = false,
            isEmailVerified = verified,
            isNewUser = true
        )
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatchingAuth {
        val url = "$baseUrl/api/collections/users/request-password-reset"
        postJson(url, mapOf("email" to email))
        Unit
    }

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        val url = "$baseUrl/api/collections/users/records/${user.uid}"
        patchJson(url, mapOf("password" to newPassword, "passwordConfirm" to newPassword))
        Unit
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        val url = "$baseUrl/api/collections/users/request-verification"
        postJson(url, mapOf("email" to user.email))
        Unit
    }

    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        if (name != null) {
            val url = "$baseUrl/api/collections/users/records/${user.uid}"
            patchJson(url, mapOf("name" to name))
            activeUser = user.copy(displayName = name)
        }
        Unit
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        val url = "$baseUrl/api/collections/users/request-email-change"
        postJson(url, mapOf("newEmail" to newEmail))
        Unit
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        login(user.email, password)
        Unit
    }

    override suspend fun loginAnonymously(): DataResult<String> = runCatchingAuth {
        val anonId = UUID.randomUUID().toString().replace("-", "").take(15)
        val email = "anon_$anonId@anon.local"
        val pass = "anon_pass_$anonId"
        val created = createUser(email, pass)
        if (created is DataResult.Success) {
            created.data.uid
        } else {
            throw IllegalStateException("Failed to create anonymous PocketBase record")
        }
    }

    override suspend fun isUserLogged(): DataResult<Boolean> = runCatchingAuth {
        activeUser != null
    }

    override suspend fun deleteUser(): DataResult<Unit> = runCatchingAuth {
        val user = activeUser ?: return DataResult.Failure(AppError.Auth.UserNotFound)
        deleteRequest("$baseUrl/api/collections/users/records/${user.uid}")
        activeUser = null
        Unit
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> {
        return DataResult.Failure(
            AppError.Auth.Generic(
                IllegalStateException("OAuth2 no PocketBase é realizado via /api/collections/users/auth-with-oauth2")
            )
        )
    }

    override suspend fun signOut(): DataResult<Unit> = runCatchingAuth {
        activeUser = null
        Unit
    }

    // ─── HTTP Helpers ─────────────────────────────────────────────────────────

    private fun postJson(urlString: String, bodyMap: Map<String, Any?>): Map<String, Any?> {
        return httpRequest("POST", urlString, bodyMap)
    }

    private fun patchJson(urlString: String, bodyMap: Map<String, Any?>): Map<String, Any?> {
        return httpRequest("PATCH", urlString, bodyMap)
    }

    private fun deleteRequest(urlString: String) {
        httpRequest("DELETE", urlString, null)
    }

    @Suppress("UNCHECKED_CAST")
    private fun httpRequest(method: String, urlString: String, bodyMap: Map<String, Any?>?): Map<String, Any?> {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            doInput = true
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }

        if (bodyMap != null) {
            conn.doOutput = true
            val json = gson.toJson(bodyMap)
            conn.outputStream.use { it.write(json.toByteArray()) }
        }

        val code = conn.responseCode
        if (code in 200..299) {
            val responseText = conn.inputStream.use { it.bufferedReader().readText() }
            return if (responseText.isNotBlank()) {
                gson.fromJson(responseText, Map::class.java) as Map<String, Any?>
            } else emptyMap()
        } else {
            throw IllegalStateException("PocketBase HTTP $code: ${conn.responseMessage}")
        }
    }

    private inline fun <T> runCatchingAuth(block: () -> T): DataResult<T> {
        return try {
            DataResult.Success(block())
        } catch (e: Exception) {
            DataResult.Failure(PocketBaseErrorMapper.mapThrowable(e, "auth"))
        }
    }
}
