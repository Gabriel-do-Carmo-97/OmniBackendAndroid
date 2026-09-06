package br.wgc.omnibackend.firebase.domain.model

/**
 * Modelo de domínio que representa os dados necessários para registrar um novo usuário.
 * Este objeto é usado pela camada de UI para se comunicar com o caso de uso.
 */
data class NewUser(
    val name: String,
    val lastName: String,
    val email: String,
    val password: String,
    val photo: String? = null,
    val isClient: Boolean = true
)

