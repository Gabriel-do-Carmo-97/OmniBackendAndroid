package br.wgc.omnibackend.core.repository

import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato agnóstico para serviços de autenticação.
 */
interface AuthRepository {
    /**
     * Fluxo reativo do estado da sessão de usuário em tempo real.
     * Emite o usuário logado ou null quando desconectado.
     */
    val authState: Flow<OmniUser?>

    /**
     * Usuário atualmente autenticado em memória (síncrono).
     */
    val currentUser: OmniUser?

    suspend fun login(email: String, pass: String): DataResult<OmniUser>
    suspend fun createUser(email: String, pass: String): DataResult<OmniUser>
    suspend fun resetPassword(email: String): DataResult<Unit>
    suspend fun sendEmailVerification(): DataResult<Unit>
    suspend fun updatePassword(newPassword: String): DataResult<Unit>
    suspend fun reauthenticate(password: String): DataResult<Unit>
    suspend fun deleteUser(): DataResult<Unit>
    suspend fun signOut(): DataResult<Unit>
}
