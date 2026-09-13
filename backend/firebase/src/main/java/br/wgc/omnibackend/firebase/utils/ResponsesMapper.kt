package br.wgc.omnibackend.firebase.utils

import android.net.Uri
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.model.auth.RegisteredUser
import com.google.firebase.auth.FirebaseUser

/**
 * Converte a entidade nativa [FirebaseUser] do Firebase Auth para o modelo agnóstico [OmniUser].
 *
 * @receiver Objeto [FirebaseUser] ou `null`.
 * @return Instância equivalente de [OmniUser], ou `null` se o receptor for nulo.
 */
fun FirebaseUser?.toOmniUser(): OmniUser? {
    if (this == null) return null
    return OmniUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        isEmailVerified = isEmailVerified,
        isAnonymous = isAnonymous,
    )
}

/**
 * Converte a resposta estruturada [RegisterUserResponse] na entidade de domínio [RegisteredUser].
 *
 * @param photo [Uri] opcional da foto do usuário.
 * @param isClient Flag indicando se a conta pertence a um cliente final.
 * @return Entidade [RegisteredUser] preenchida.
 */
fun RegisterUserResponse.toRegisteredUser(photo: Uri? = null, isClient: Boolean = true): RegisteredUser = RegisteredUser(
    id = this.id.orEmpty(),
    completedName = this.name.orEmpty(),
    email = this.email.orEmpty(),
    photo = photo,
    method = this.method.orEmpty(),
    provider = this.provider.orEmpty(),
    isAnonymous = this.isAnonymous ?: false,
    isEmailVerified = this.isEmailVerified ?: false,
    isNewUser = this.isNewUser ?: false,
    isClient = isClient,
)
