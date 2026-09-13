package br.wgc.omnibackend.core.model.auth

/**
 * Resposta de sucesso retornada após a conclusão do registro de um novo usuário na plataforma.
 *
 * @property id Identificador único gerado para o usuário cadastrado.
 * @property name Nome fornecido durante o cadastro.
 * @property email Endereço de e-mail registrado.
 * @property method Método de autenticação utilizado (ex: "password", "google.com", "phone").
 * @property provider Provedor de identidade responsável (ex: "firebase", "supabase", "appwrite").
 * @property isAnonymous Indica se o usuário foi criado como uma sessão anônima.
 * @property isEmailVerified Indica se o e-mail cadastrado já se encontra verificado.
 * @property isNewUser Indica se esta é a primeira vez que o usuário é registrado no sistema.
 */
data class RegisterUserResponse(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val method: String? = null,
    val provider: String? = null,
    val isAnonymous: Boolean? = false,
    val isEmailVerified: Boolean? = false,
    val isNewUser: Boolean? = false,
)
