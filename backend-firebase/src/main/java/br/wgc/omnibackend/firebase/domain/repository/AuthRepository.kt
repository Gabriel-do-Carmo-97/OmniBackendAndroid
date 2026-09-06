package br.wgc.omnibackend.firebase.domain.repository

import android.net.Uri
import br.wgc.omnibackend.firebase.data.model.auth.RegisterUserResponse
import br.wgc.omnibackend.firebase.utils.DataResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<FirebaseUser?>

    suspend fun registerEmailWithPassword(email: String, password: String): DataResult<RegisterUserResponse>
    suspend fun loginEmailWithPassword(email: String, password: String): DataResult<String>
    suspend fun getCurrentUser(): DataResult<FirebaseUser?>
    suspend fun signOut(): DataResult<Unit>
    suspend fun sendPasswordResetEmail(email: String): DataResult<Unit>


    suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit>
    suspend fun updateEmail(newEmail: String): DataResult<Unit>
    suspend fun updatePassword(newPassword: String): DataResult<Unit>
    suspend fun delete(): DataResult<Unit>
    suspend fun reauthenticate(password: String): DataResult<Unit>
    suspend fun sendEmailVerification(): DataResult<Unit>


    suspend fun linkWithCredential(email: String, credential: AuthCredential): DataResult<Unit>
    suspend fun unlink(providerId: String): DataResult<Unit>
    suspend fun loginAnonymously(): DataResult<String>
    suspend fun isUserLogged(): DataResult<Boolean>
    suspend fun loginWithCredential(credential: AuthCredential): DataResult<String>
    suspend fun signInWithGoogle(credential: AuthCredential): DataResult<AuthResult>//    suspend fun loginWithFacebook(accessToken: String)
//    suspend fun loginWithTwitter(authToken: String, authSecret: String)
//    suspend fun loginWithGithub(accessToken: String)
//    suspend fun loginWithApple(idToken: String)
//    suspend fun loginWithMicrosoft(accessToken: String)
//    suspend fun loginWithYahoo(accessToken: String)
//    suspend fun loginWithPlayGames(accessToken: String)
//    suspend fun loginWithPhoneNumber(number: String)
}

