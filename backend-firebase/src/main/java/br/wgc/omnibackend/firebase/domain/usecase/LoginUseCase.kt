package br.wgc.omnibackend.firebase.domain.usecase

import androidx.credentials.GetCredentialRequest
import br.wgc.omnibackend.firebase.domain.repository.AuthRepository
import br.wgc.omnibackend.firebase.domain.repository.RealtimeDatabaseRepository
import br.wgc.omnibackend.firebase.utils.AppError
import br.wgc.omnibackend.firebase.utils.DataResult
import br.wgc.omnibackend.firebase.utils.UseCaseResult
import br.wgc.omnibackend.firebase.utils.UseCaseResult.Failure
import br.wgc.omnibackend.firebase.utils.UseCaseResult.Loading
import br.wgc.omnibackend.firebase.utils.UseCaseResult.Success
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Caso de uso para executar a lógica de login de um usuário.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val databaseRepository: RealtimeDatabaseRepository,
) {
    /**
     * Executa o caso de uso de login.
     * Retorna um Flow que emite o estado da operação.
     *
     * @param email Email do usuário
     * @param password Senha do usuário
     * @param updatePresence Se verdadeiro, marca presença online no Realtime Database. Padrão: false.
     * @return Um Flow que emite UseCaseResult (Loading, Success, Failure).
     */
    operator fun invoke(
        email: String,
        password: String,
        updatePresence: Boolean = false,
    ): Flow<UseCaseResult<String>> = flow {
        emit(Loading)

        when (val authResult = authRepository.loginEmailWithPassword(email, password)) {
            is DataResult.Success -> {
                if (updatePresence) {
                    val presenceResult = databaseRepository
                        .presence()
                        .goOnline(
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
     * Executa o caso de uso de login anônimo.
     *
     * @param updatePresence Se verdadeiro, marca presença online no Realtime Database. Padrão: false.
     * @return Um Flow que emite o estado da operação.
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
     * Cria a requisição para o Credential Manager do Google.
     * Esta função prepara a solicitação para a UI, que irá executá-la.
     *
     * @param serverClientId O Web Client ID do seu projeto Firebase/Google Cloud.
     *                       Encontrado no seu arquivo google-services.json (client_type: 3).
     */
    fun createGoogleSignInRequest(serverClientId: String): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Permite escolher qualquer conta Google
            .setServerClientId(serverClientId)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    /**
     * Processa o resultado do login com Google, autentica no Firebase e atualiza o status de presença.
     *
     * @param credential A credencial obtida pela UI através do Credential Manager.
     * @return Um Flow que emite o estado da operação (Loading, Success, Failure).
     */
    fun handleGoogleSignInSuccess(
        credential: GoogleIdTokenCredential,
        updatePresence: Boolean = false
    ): Flow<UseCaseResult<String>> = flow {
        emit(Loading)

        val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)

        when (val signInResult = authRepository.signInWithGoogle(firebaseCredential)) {
            is DataResult.Success -> {
                val userId = signInResult.data.user?.uid
                if (userId == null) {
                    emit(Failure(AppError.Generic.GenericException("Falha ao obter o ID do usuário após o login.")))
                    return@flow
                }

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

