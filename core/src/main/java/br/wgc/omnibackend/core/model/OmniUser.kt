package br.wgc.omnibackend.core.model

/**
 * Representação agnóstica e imutável de um usuário autenticado no ecossistema OmniBackend.
 *
 * Desacopla a aplicação cliente de fornecedores de identidade específicos (Firebase Auth,
 * Supabase GoTrue, Appwrite Account, etc.), fornecendo uma entidade canônica e padronizada.
 *
 * @property uid Identificador único e persistente do usuário atribuído pelo provedor de autenticação.
 * @property email Endereço de e-mail do usuário, ou `null` caso tenha sido autenticado via biometria/telefone/anônimo.
 * @property displayName Nome de exibição público ou nome completo cadastrado pelo usuário.
 * @property photoUrl URL pública da imagem de avatar/foto de perfil do usuário.
 * @property isEmailVerified Indica se o endereço de e-mail associado foi formalmente verificado pelo usuário.
 * @property isAnonymous Indica se a sessão atual é anônima (convidado / guest session).
 */
data class OmniUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val isAnonymous: Boolean = false,
) {
    /** Alias ergonômico para [uid]. */
    val id: String get() = uid
}
