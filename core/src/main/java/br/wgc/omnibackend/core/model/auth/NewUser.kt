package br.wgc.omnibackend.core.model.auth

/**
 * Modelo de domínio que encapsula os dados fornecidos pela camada de apresentação
 * para a criação de uma nova conta de usuário.
 *
 * @property name Primeiro nome do usuário.
 * @property lastName Sobrenome do usuário.
 * @property email Endereço de e-mail a ser registrado.
 * @property password Senha em texto puro a ser criptografada e enviada ao provedor.
 * @property photo URL opcional para a imagem de avatar do perfil inicial.
 * @property isClient Flag indicando se a conta criada possui o papel de cliente final.
 */
data class NewUser(
    val name: String,
    val lastName: String,
    val email: String,
    val password: String,
    val photo: String? = null,
    val isClient: Boolean = true
)
