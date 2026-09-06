package br.wgc.omnibackend.firebase.domain.usecase

import androidx.credentials.GetCredentialRequest
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.RealtimeDatabaseRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.firebase.utils.UseCaseResult
import br.wgc.omnibackend.firebase.utils.UseCaseResult.Failure
import br.wgc.omnibackend.firebase.utils.UseCaseResult.Loading
import br.wgc.omnibackend.firebase.utils.UseCaseResult.Success
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Caso de uso corporativo para autenticação e gerenciamento de presença de usuários.
 *
 * Suporta autenticação por e-mail/senha, credenciais anônimas e Google Sign-In via Credential Manager.
 *
 * @property authRepository Contrato agnóstico de autenticação do módulo `:core`.
 * @property databaseRepository Repositório do Realtime Database para sincronização de presença.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val databaseRepository: RealtimeDatabaseRepository,
) {
    /**
     * Executa a autenticação com e-mail e senha.
     *
     * @param email Endereço de e-mail do usuário.
     * @param password Senha secreta de acesso.
     * @param updatePresence Se verdadeiro, marca presença online no Realtime Database. Padrão: false.
     * @return [Flow] reativo que emite [UseCaseResult] contendo o ID do usuário em caso de sucesso.
     */
    operator fun invoke(
        email: String,
        password: String,
        updatePresence: Boolean = false,
    ): Flow<UseCaseResult<String>> = flow {
        emit(Loading)

        when (val authResult = authRepository.login(email, password)) {
            is DataResult.Success -> {
                val userId = authResult.data.id
                if (updatePresence) {
                    val presenceResult = databaseRepository
                        .presence()
                        .goOnline(
                            entityType = "users",
                            entityId = userId
                        )
                    if (presenceResult is DataResult.Failure) {
                        authRepository.signOut()
                        emit(Failure(presenceResult.error))
                        return@flow
                    }
                }
                emit(Success(userId))
            }

            is DataResult.Failure -> emit(Failure(authResult.error))
        }
    }

    /**
     * Autentica uma sessão temporária anônima (convidado).
     *
     * @param updatePresence Se verdadeiro, marca presença online no Realtime Database. Padrão: false.
     * @return [Flow] que emite [UseCaseResult] contendo o identificador anônimo gerado.
     */
    fun loginAnonymous(updatePresence: Boolean = false): Flow<UseCaseResult<String>> = flow {
        emit(Loading)

        when (val authResult = authRepository.loginAnonymously()) {
            is DataResult.Success<String> -> {
                if (updatePresence) {
                    val presenceResult = databaseRepository.presence().goOnline(
                        entityType = "users",
                        entityId = authResult.data
                    )
                    if (presenceResult is DataResult.Failure) {
                        authRepository.signOut()
                        emit(Failure(presenceResult.error))
                        return@flow
                    }
                }
                emit(Success(authResult.data))
            }
            is DataResult.Failure -> emit(Failure(authResult.error))
        }
    }

    /**
     * Constrói a solicitação para o Android Credential Manager com Google ID Option.
     *
     * @param serverClientId Web Client ID obtido do console do Firebase / Google Cloud.
     * @return [GetCredentialRequest] configurado pronto para ser disparado pelo Credential Manager.
     */
    fun createGoogleSignInRequest(serverClientId: String): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    /**
     * Processa o token de identidade emitido pelo Credential Manager e autentica o usuário.
     *
     * @param credential Credencial emitida com o token ID do Google.
     * @param updatePresence Se verdadeiro, marca presença online no Realtime Database. Padrão: false.
     * @return [Flow] reativo que emite [UseCaseResult] contendo o ID do usuário autenticado.
     */
    fun handleGoogleSignInSuccess(
        credential: GoogleIdTokenCredential,
        updatePresence: Boolean = false
    ): Flow<UseCaseResult<String>> = flow {
        emit(Loading)

        when (val signInResult = authRepository.signInWithGoogle(credential.idToken)) {
            is DataResult.Success -> {
                val userId = signInResult.data.id

                if (updatePresence) {
                    val presenceResult = databaseRepository.presence().goOnline(
                        entityType = "users",
                        entityId = userId
                    )

                    if (presenceResult is DataResult.Failure) {
                        authRepository.signOut()
                        emit(Failure(presenceResult.error))
                        return@flow
                    }
                }
                emit(Success(userId))
            }
            is DataResult.Failure -> {
                emit(Failure(signInResult.error))
            }
        }
    }
}

