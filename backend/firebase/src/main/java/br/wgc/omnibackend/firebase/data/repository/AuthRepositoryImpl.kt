package br.wgc.omnibackend.firebase.data.repository

import android.net.Uri
import android.util.Log
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.firebase.utils.toOmniUser
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

/**
 * Implementação do contrato [AuthRepository] utilizando o SDK oficial do Firebase Authentication.
 *
 * Converte todas as exceções nativas do Firebase em [AppError] e expõe entidades agnósticas [OmniUser].
 *
 * @property auth Instância do [FirebaseAuth] utilizada para as chamadas de API.
 */
class AuthRepositoryImpl @Inject constructor(private val auth: FirebaseAuth) : AuthRepository {

    /**
     * Fluxo reativo do estado de autenticação em tempo real emitindo [OmniUser] ou `null`.
     */
    override val authState: Flow<OmniUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser.toOmniUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Retorna o usuário autenticado em memória ou `null` caso desconectado.
     */
    override val currentUser: OmniUser?
        get() = auth.currentUser.toOmniUser()

    /**
     * Autentica o usuário com e-mail e senha no Firebase Auth.
     *
     * @param email Endereço de e-mail do usuário.
     * @param pass Senha secreta de acesso.
     * @return [DataResult.Success] contendo a entidade [OmniUser] em caso de autenticação válida.
     */
    override suspend fun login(email: String, pass: String): DataResult<OmniUser> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, pass).await()
        val user = authResult.user?.toOmniUser()
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Usuário não encontrado.")
        DataResult.Success(user)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha no login: ${exception.message}", exception)
        val error: AppError = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Cria um novo usuário no Firebase Auth utilizando e-mail e senha.
     *
     * @param email Endereço de e-mail para registro.
     * @param pass Senha secreta.
     * @return [DataResult.Success] com a entidade [OmniUser] criada.
     */
    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
        val user = authResult.user?.toOmniUser()
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Falha ao criar usuário.")
        DataResult.Success(user)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha na criação de usuário: ${exception.message}", exception)
        val error: AppError = when (exception) {
            is FirebaseAuthWeakPasswordException -> AppError.Auth.WeakPassword
            is FirebaseAuthUserCollisionException -> AppError.Auth.EmailAlreadyInUse
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is IOException -> AppError.Generic.Network
            is FirebaseAuthException -> AppError.Auth.Generic(exception)
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Registra o usuário com e-mail e senha e retorna o resumo estruturado [RegisterUserResponse].
     *
     * @param email Endereço de e-mail.
     * @param pass Senha de acesso.
     * @return [DataResult.Success] com o [RegisterUserResponse].
     */
    override suspend fun registerEmailWithPassword(email: String, pass: String): DataResult<RegisterUserResponse> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
        authResult.user?.sendEmailVerification()?.await()
        DataResult.Success(
            RegisterUserResponse(
                id = authResult.user?.uid.toString(),
                name = authResult.additionalUserInfo?.username.toString(),
                method = authResult.credential?.signInMethod.toString(),
                provider = authResult.credential?.provider.toString(),
                isAnonymous = authResult.user?.isAnonymous ?: false,
                isEmailVerified = authResult.user?.isEmailVerified ?: false,
                isNewUser = authResult.additionalUserInfo?.isNewUser ?: false,
            ),
        )
    }.getOrElse { exception ->
        Log.e(TAG, "Falha no registro detalhado: ${exception.message}", exception)
        val error: AppError = when (exception) {
            is FirebaseAuthWeakPasswordException -> AppError.Auth.WeakPassword
            is FirebaseAuthUserCollisionException -> AppError.Auth.EmailAlreadyInUse
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is IOException -> AppError.Generic.Network
            is FirebaseAuthException -> AppError.Auth.Generic(exception)
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Envia e-mail de redefinição de senha para o endereço fornecido.
     *
     * @param email E-mail cadastrado.
     * @return [DataResult.Success] com [Unit] após o disparo do e-mail.
     */
    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha ao enviar e-mail de redefinição: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Dispara e-mail de validação para o usuário logado atualmente.
     */
    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.sendEmailVerification().await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha ao enviar verificação de e-mail: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Atualiza a senha da conta ativa.
     *
     * @param newPassword Nova senha desejada.
     */
    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.updatePassword(newPassword).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha ao atualizar senha: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is FirebaseAuthRecentLoginRequiredException -> AppError.Auth.RequiresRecentLogin
            is FirebaseAuthWeakPasswordException -> AppError.Auth.WeakPassword
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Atualiza o perfil (nome e avatar) do usuário logado.
     *
     * @param name Novo nome de exibição.
     * @param photoUri URI da nova foto.
     */
    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")

        val request = UserProfileChangeRequest.Builder().apply {
            name?.let { setDisplayName(it) }
            photoUri?.let { setPhotoUri(it) }
        }.build()

        user.updateProfile(request).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha ao atualizar perfil: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Atualiza o e-mail do usuário ativo no Firebase.
     *
     * @param newEmail Novo endereço de e-mail.
     */
    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.verifyBeforeUpdateEmail(newEmail).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha ao atualizar e-mail: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is FirebaseAuthRecentLoginRequiredException -> AppError.Auth.RequiresRecentLogin
            is FirebaseAuthUserCollisionException -> AppError.Auth.EmailAlreadyInUse
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Reautentica a sessão ativa com a senha atual para validação de segurança.
     *
     * @param password Senha atual.
     */
    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        val email = user.email
            ?: throw FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIALS", "E-mail do usuário não disponível.")
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha na reautenticação: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Autentica uma nova sessão anônima de convidado.
     */
    override suspend fun loginAnonymously(): DataResult<String> = runCatching {
        val authResult = auth.signInAnonymously().await()
        val uid = authResult.user?.uid
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Usuário anônimo não encontrado.")
        DataResult.Success(uid)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha no login anônimo: ${exception.message}", exception)
        val error = when (exception) {
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Verifica se existe um usuário autenticado ativo no momento.
     */
    override suspend fun isUserLogged(): DataResult<Boolean> = runCatching {
        DataResult.Success(auth.currentUser != null)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha ao checar status de login: ${exception.message}", exception)
        val error = when (exception) {
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Deleta a conta do usuário autenticado no Firebase.
     */
    override suspend fun deleteUser(): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.delete().await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha ao deletar usuário: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is FirebaseAuthRecentLoginRequiredException -> AppError.Auth.RequiresRecentLogin
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Autentica no Firebase utilizando o Google ID Token fornecido pelo Credential Manager.
     *
     * @param idToken Token de identidade JWT retornado pela autenticação do Google.
     * @return [DataResult.Success] com [OmniUser] ou [DataResult.Failure] com erro mapeado.
     */
    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val user = authResult.user?.toOmniUser()
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Usuário não encontrado após login Google.")
        DataResult.Success(user)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha no signInWithGoogle: ${exception.message}", exception)
        val error: AppError = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    /**
     * Encerra a sessão ativa deslogando o usuário do dispositivo.
     */
    override suspend fun signOut(): DataResult<Unit> = runCatching {
        auth.signOut()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e(TAG, "Falha ao deslogar: ${exception.message}", exception)
        DataResult.Failure(AppError.Generic.Unknown(exception))
    }

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }
}
