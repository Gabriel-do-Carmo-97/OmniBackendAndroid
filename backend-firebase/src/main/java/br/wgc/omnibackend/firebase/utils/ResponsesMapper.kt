package br.wgc.omnibackend.firebase.utils

import android.net.Uri
import br.wgc.omnibackend.firebase.data.model.auth.RegisterUserResponse
import br.wgc.omnibackend.firebase.domain.model.RegisteredUser

fun RegisterUserResponse.toRegisteredUser(
    photo: Uri? = null,
    isClient: Boolean = true
) = RegisteredUser(
    id = this.id.orEmpty(),
    completedName = this.name.orEmpty(),
    email = this.email.orEmpty(),
    photo = photo,
    method = this.method.orEmpty(),
    provider = this.provider.orEmpty(),
    isAnonymous = this.isAnonymous ?: false,
    isEmailVerified = this.isEmailVerified ?: false,
    isNewUser = this.isNewUser ?: false,
    isClient = isClient

)
