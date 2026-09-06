package br.wgc.omnibackend.firebase.domain.model

import android.net.Uri

/**
 * Modelo de domínio que representa um usuário que foi registrado com sucesso.
 * Este objeto é retornado pelo caso de uso para a camada de UI.
 */
data class RegisteredUser(
    val id: String,
    val completedName: String,
    val email: String,
    val photo: Uri? = null,
    val method: String,
    val provider: String,
    val isAnonymous: Boolean,
    val isEmailVerified: Boolean,
    val isNewUser: Boolean,
    val isClient: Boolean
)

