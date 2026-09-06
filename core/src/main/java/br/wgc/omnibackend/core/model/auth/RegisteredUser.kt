package br.wgc.omnibackend.core.model.auth

import android.net.Uri

/**
 * Modelo de domínio representando um usuário registrado com sucesso, retornado por casos de uso.
 *
 * @property id Identificador único persistente do usuário.
 * @property completedName Nome completo concatenado do usuário.
 * @property email Endereço de e-mail cadastrado.
 * @property photo [Uri] opcional apontando para a foto de perfil.
 * @property method Método de autenticação utilizado no registro.
 * @property provider Provedor de identidade responsável pela criação da credencial.
 * @property isAnonymous Indica se a sessão é temporária/anônima.
 * @property isEmailVerified Indica se o e-mail cadastrado já foi validado.
 * @property isNewUser Indica se a conta acabou de ser criada nesta operação.
 * @property isClient Indica se a conta pertence a um cliente final.
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
