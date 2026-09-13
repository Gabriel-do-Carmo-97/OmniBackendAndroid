package br.wgc.omnibackend.testing

import android.net.Uri
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Implementação em memória de [AuthRepository] projetada especificamente para testes unitários
 * e integrados sem dependência de serviços de nuvem ou emuladores.
 */
class FakeAuthRepository(initialUser: OmniUser? = null) : AuthRepository {

    private val _authState = MutableStateFlow<OmniUser?>(initialUser)
    override val authState: Flow<OmniUser?> = _authState.asStateFlow()

    override val currentUser: OmniUser?
        get() = _authState.value

    private val usersDb = mutableMapOf<String, Pair<OmniUser, String>>()

    /** Permite injetar um erro simulado para testar fluxos de falha no consumidor. */
    var simulatedError: AppError? = null

    init {
        if (initialUser != null && initialUser.email != null) {
            usersDb[initialUser.email!!] = Pair(initialUser, "default_pass")
        }
    }

    override suspend fun login(email: String, pass: String): DataResult<OmniUser> {
        simulatedError?.let { return DataResult.Failure(it) }
        val userRecord = usersDb[email]
        return if (userRecord != null && userRecord.second == pass) {
            _authState.value = userRecord.first
            DataResult.Success(userRecord.first)
        } else {
            DataResult.Failure(AppError.Auth.InvalidCredentials)
        }
    }

    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> {
        simulatedError?.let { return DataResult.Failure(it) }
        if (usersDb.containsKey(email)) {
            return DataResult.Failure(AppError.Auth.EmailAlreadyInUse)
        }
        val newUser = OmniUser(
            uid = UUID.randomUUID().toString(),
            email = email,
            displayName = email.substringBefore("@"),
            photoUrl = null,
            isEmailVerified = false,
            isAnonymous = false,
        )
        usersDb[email] = Pair(newUser, pass)
        _authState.value = newUser
        return DataResult.Success(newUser)
    }

    override suspend fun registerEmailWithPassword(email: String, pass: String): DataResult<RegisterUserResponse> {
        val result = createUser(email, pass)
        return when (result) {
            is DataResult.Success -> DataResult.Success(
                RegisterUserResponse(
                    id = result.data.uid,
                    name = result.data.displayName,
                    email = result.data.email,
                    method = "password",
                    provider = "fake",
                    isAnonymous = false,
                    isEmailVerified = result.data.isEmailVerified,
                    isNewUser = true,
                ),
            )
            is DataResult.Failure -> DataResult.Failure(result.error)
        }
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        return if (usersDb.containsKey(email)) {
            DataResult.Success(Unit)
        } else {
            DataResult.Failure(AppError.Auth.UserNotFound)
        }
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        return if (_authState.value != null) {
            DataResult.Success(Unit)
        } else {
            DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        }
    }

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        val current = _authState.value ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        val email = current.email ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        usersDb[email] = Pair(current, newPassword)
        return DataResult.Success(Unit)
    }

    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        val current = _authState.value ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        val email = current.email ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        val updated = current.copy(
            displayName = name ?: current.displayName,
            photoUrl = photoUri?.toString() ?: current.photoUrl,
        )
        val pass = usersDb[email]?.second ?: ""
        usersDb[email] = Pair(updated, pass)
        _authState.value = updated
        return DataResult.Success(Unit)
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        val current = _authState.value ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        val email = current.email ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        val pass = usersDb.remove(email)?.second ?: ""
        val updated = current.copy(email = newEmail)
        usersDb[newEmail] = Pair(updated, pass)
        _authState.value = updated
        return DataResult.Success(Unit)
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        val current = _authState.value ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        val email = current.email ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        val record = usersDb[email]
        return if (record != null && record.second == password) {
            DataResult.Success(Unit)
        } else {
            DataResult.Failure(AppError.Auth.InvalidCredentials)
        }
    }

    override suspend fun loginAnonymously(): DataResult<String> {
        simulatedError?.let { return DataResult.Failure(it) }
        val anonId = "anon_${UUID.randomUUID()}"
        val anonUser = OmniUser(
            uid = anonId,
            displayName = "Guest",
            email = "$anonId@guest.local",
            photoUrl = null,
            isEmailVerified = false,
            isAnonymous = true,
        )
        _authState.value = anonUser
        return DataResult.Success(anonId)
    }

    override suspend fun isUserLogged(): DataResult<Boolean> {
        simulatedError?.let { return DataResult.Failure(it) }
        return DataResult.Success(_authState.value != null)
    }

    override suspend fun deleteUser(): DataResult<Unit> {
        simulatedError?.let { return DataResult.Failure(it) }
        val current = _authState.value ?: return DataResult.Failure(AppError.Auth.RequiresRecentLogin)
        current.email?.let { usersDb.remove(it) }
        _authState.value = null
        return DataResult.Success(Unit)
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> {
        simulatedError?.let { return DataResult.Failure(it) }
        val user = OmniUser(
            uid = "google_${UUID.randomUUID()}",
            displayName = "Google User",
            email = "user@gmail.com",
            photoUrl = null,
            isEmailVerified = true,
            isAnonymous = false,
        )
        usersDb[user.email!!] = Pair(user, "")
        _authState.value = user
        return DataResult.Success(user)
    }

    override suspend fun signOut(): DataResult<Unit> {
        _authState.value = null
        return DataResult.Success(Unit)
    }
}
