package br.wgc.omnibackend.core.hybrid

import android.net.Uri
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Repositório híbrido de autenticação com suporte a **Circuit Breaker / Failover automático**.
 *
 * Tenta executar as operações prioritariamente no [primaryRepository]. Se o provedor primário
 * falhar (retornando [DataResult.Failure]), o [HybridAuthRepository] executa a operação no
 * [fallbackRepository] sem interromper a experiência do usuário.
 *
 * @param primaryRepository Provedor primário de autenticação (ex: Firebase).
 * @param fallbackRepository Provedor secundário de autenticação (ex: Supabase, Appwrite, PocketBase).
 */
class HybridAuthRepository(
    private val primaryRepository: AuthRepository,
    private val fallbackRepository: AuthRepository
) : AuthRepository {

    override val authState: Flow<OmniUser?>
        get() = primaryRepository.authState

    override val currentUser: OmniUser?
        get() = primaryRepository.currentUser ?: fallbackRepository.currentUser

    override suspend fun login(email: String, pass: String): DataResult<OmniUser> {
        val result = primaryRepository.login(email, pass)
        return if (result is DataResult.Success) result else fallbackRepository.login(email, pass)
    }

    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> {
        val result = primaryRepository.createUser(email, pass)
        return if (result is DataResult.Success) result else fallbackRepository.createUser(email, pass)
    }

    override suspend fun registerEmailWithPassword(
        email: String,
        pass: String
    ): DataResult<RegisterUserResponse> {
        val result = primaryRepository.registerEmailWithPassword(email, pass)
        return if (result is DataResult.Success) result else fallbackRepository.registerEmailWithPassword(email, pass)
    }

    override suspend fun resetPassword(email: String): DataResult<Unit> {
        val result = primaryRepository.resetPassword(email)
        return if (result is DataResult.Success) result else fallbackRepository.resetPassword(email)
    }

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> {
        val result = primaryRepository.updatePassword(newPassword)
        return if (result is DataResult.Success) result else fallbackRepository.updatePassword(newPassword)
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> {
        val result = primaryRepository.sendEmailVerification()
        return if (result is DataResult.Success) result else fallbackRepository.sendEmailVerification()
    }

    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> {
        val result = primaryRepository.updateProfile(name, photoUri)
        return if (result is DataResult.Success) result else fallbackRepository.updateProfile(name, photoUri)
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> {
        val result = primaryRepository.updateEmail(newEmail)
        return if (result is DataResult.Success) result else fallbackRepository.updateEmail(newEmail)
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> {
        val result = primaryRepository.reauthenticate(password)
        return if (result is DataResult.Success) result else fallbackRepository.reauthenticate(password)
    }

    override suspend fun loginAnonymously(): DataResult<String> {
        val result = primaryRepository.loginAnonymously()
        return if (result is DataResult.Success) result else fallbackRepository.loginAnonymously()
    }

    override suspend fun isUserLogged(): DataResult<Boolean> {
        val result = primaryRepository.isUserLogged()
        return if (result is DataResult.Success) result else fallbackRepository.isUserLogged()
    }

    override suspend fun deleteUser(): DataResult<Unit> {
        val result = primaryRepository.deleteUser()
        return if (result is DataResult.Success) result else fallbackRepository.deleteUser()
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> {
        val result = primaryRepository.signInWithGoogle(idToken)
        return if (result is DataResult.Success) result else fallbackRepository.signInWithGoogle(idToken)
    }

    override suspend fun signOut(): DataResult<Unit> {
        val primaryResult = primaryRepository.signOut()
        val fallbackResult = fallbackRepository.signOut()
        return if (primaryResult is DataResult.Success) primaryResult else fallbackResult
    }
}
