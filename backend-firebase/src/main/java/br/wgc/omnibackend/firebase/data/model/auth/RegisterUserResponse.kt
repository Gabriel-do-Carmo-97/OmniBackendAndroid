package br.wgc.omnibackend.firebase.data.model.auth

data class RegisterUserResponse(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val method: String? = null,
    val provider: String? = null,
    val isAnonymous: Boolean? = false,
    val isEmailVerified: Boolean? = false,
    val isNewUser: Boolean? = false
)
