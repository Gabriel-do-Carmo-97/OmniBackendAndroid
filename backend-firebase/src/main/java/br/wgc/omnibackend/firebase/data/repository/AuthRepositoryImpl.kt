package br.wgc.omnibackend.firebase.data.repository

import android.net.Uri
import android.util.Log
import br.wgc.omnibackend.firebase.data.model.auth.RegisterUserResponse
import br.wgc.omnibackend.firebase.domain.repository.AuthRepository
import br.wgc.omnibackend.firebase.utils.AppError
import br.wgc.omnibackend.firebase.utils.DataResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

internal class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val authState: Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun registerEmailWithPassword(
        email: String,
        password: String
    ): DataResult<RegisterUserResponse> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(
            email,
            password
        ).await()

        authResult.user?.sendEmailVerification()?.await()
        DataResult.Success(
            RegisterUserResponse(
                id = authResult.user?.uid.toString(),
                name = authResult.additionalUserInfo?.username.toString(),
                method = authResult.credential?.signInMethod.toString(),
                provider = authResult.credential?.provider.toString(),
                isAnonymous = authResult.user?.isAnonymous ?: false,
                isEmailVerified = authResult.user?.isEmailVerified ?: false,
                isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            )
        )
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha no registro: ${exception.message}", exception)
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

    override suspend fun loginEmailWithPassword(
        email: String,
        password: String
    ): DataResult<String> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(
            email,
            password
        ).await()
        val uid = authResult.user?.uid
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Usuário não encontrado.")
        DataResult.Success(uid)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha no login: ${exception.message}", exception)
        val error: AppError = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun getCurrentUser(): DataResult<com.google.firebase.auth.FirebaseUser?> = runCatching {
        DataResult.Success(auth.currentUser)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao obter usuário atual: ${exception.message}", exception)
        val error: AppError = when (exception) {
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun isUserLogged(): DataResult<Boolean> = runCatching {
        DataResult.Success(auth.currentUser != null)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao verificar usuário logado: ${exception.message}", exception)
        val error: AppError = when (exception) {
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun signOut(): DataResult<Unit> = runCatching {
        auth.signOut()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao deslogar: ${exception.message}", exception)
        DataResult.Failure(AppError.Generic.Unknown(exception))
    }

    override suspend fun updateProfile(
        name: String?,
        photoUri: Uri?
    ): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException(
                "ERROR_USER_NOT_FOUND",
                "Nenhum usuário logado."
            )

        val request = UserProfileChangeRequest.Builder().apply {
            name?.let { setDisplayName(it) }
            photoUri?.let { setPhotoUri(it) }
        }.build()

        user.updateProfile(request).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao atualizar perfil: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.verifyBeforeUpdateEmail(newEmail).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao atualizar e-mail: ${exception.message}", exception)
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

    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.updatePassword(newPassword).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao atualizar senha: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is FirebaseAuthRecentLoginRequiredException -> AppError.Auth.RequiresRecentLogin
            is FirebaseAuthWeakPasswordException -> AppError.Auth.WeakPassword
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.sendEmailVerification().await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao enviar verificação de e-mail: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun sendPasswordResetEmail(email: String): DataResult<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao enviar e-mail de redefinição: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun delete(): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.delete().await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao deletar usuário: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is FirebaseAuthRecentLoginRequiredException -> AppError.Auth.RequiresRecentLogin
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        val email = user.email
            ?: throw FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIALS", "E-mail do usuário não disponível.")
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha na reautenticação: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun linkWithCredential(email: String, credential: AuthCredential): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.linkWithCredential(credential).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao vincular credencial: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthUserCollisionException -> AppError.Auth.EmailAlreadyInUse
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is FirebaseAuthRecentLoginRequiredException -> AppError.Auth.RequiresRecentLogin
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun unlink(providerId: String): DataResult<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Nenhum usuário logado.")
        user.unlink(providerId).await()
        DataResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha ao desvincular provedor: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun loginWithCredential(
        credential: AuthCredential
    ): DataResult<String> = runCatching {
        val authResult = auth.signInWithCredential(credential).await()
        val uid = authResult.user?.uid
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Usuário não encontrado.")
        DataResult.Success(uid)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha no login com credencial: ${exception.message}", exception)
        val error = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AppError.Auth.InvalidCredentials
            is FirebaseAuthInvalidUserException -> AppError.Auth.UserNotFound
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun signInWithGoogle(
        credential: AuthCredential
    ): DataResult<AuthResult> = runCatching {
        val authResult = auth.signInWithCredential(credential).await()
        if (authResult.user != null) {
            DataResult.Success(authResult)
        } else {
            DataResult.Failure(AppError.Auth.UserNotFound)
        }
    }.getOrElse { exception ->
        val error = when (exception) {
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }

    override suspend fun loginAnonymously(): DataResult<String> = runCatching {
        val authResult = auth.signInAnonymously().await()
        val uid = authResult.user?.uid
            ?: throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "Usuário não encontrado.")
        DataResult.Success(uid)
    }.getOrElse { exception ->
        Log.e("AuthRepoImpl", "Falha no login anônimo: ${exception.message}", exception)
        val error = when (exception) {
            is IOException -> AppError.Generic.Network
            else -> AppError.Generic.Unknown(exception)
        }
        DataResult.Failure(error)
    }
}

